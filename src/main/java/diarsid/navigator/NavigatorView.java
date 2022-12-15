package diarsid.navigator;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import diarsid.filesystem.api.Directory;
import diarsid.filesystem.api.FSEntry;
import diarsid.filesystem.api.File;
import diarsid.navigator.model.Tab;
import diarsid.navigator.model.Tabs;
import diarsid.navigator.view.FilesView;
import diarsid.navigator.view.breadcrumbs.PathBreadcrumbsBar;
import diarsid.navigator.view.dragdrop.DragAndDropNodes;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.navigator.view.fsentry.contextmenu.FSEntryContextMenuFactory;
import diarsid.navigator.view.icons.Icons;
import diarsid.navigator.view.table.FilesTable;
import diarsid.navigator.view.table.FilesTableItem;
import diarsid.navigator.view.tabs.TabsPanel;
import diarsid.navigator.view.tree.DirectoriesTree;
import diarsid.navigator.view.tree.DirectoriesTreeCell;
import diarsid.support.javafx.controls.FrameSelection;
import diarsid.support.objects.references.Possible;

import static javafx.stage.StageStyle.DECORATED;

import static diarsid.navigator.Navigator.NAVIGATOR_FILE_SYSTEM;
import static diarsid.navigator.Navigator.NAVIGATOR_IGNORES;

class NavigatorView {

    private final Tabs tabs;
    private final Icons icons;
    private final DirectoriesTree directoriesTree;
    private final FilesTable filesTable;
    private final TabsPanel tabsPanel;

    NavigatorView() {
        Stage stage = new Stage();
        stage.initStyle(DECORATED);

        DragAndDropNodes<Label> dragAndDropLabels = new DragAndDropNodes<>("tab");
        this.icons = Icons.INSTANCE;
        this.tabs = new Tabs();

        Map<Class<? extends Node>, String> classes = new HashMap<>();
        classes.put(Label.class, "label-at-tab");
        classes.put(DirectoriesTreeCell.class, "directories-tree-cell");
        DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles = new DragAndDropObjectTransfer<>(
                "drag-and-drop-files",
                classes);

        FrameSelection frameSelection = new FrameSelection();

        FSEntryContextMenuFactory contextMenuFactory = new FSEntryContextMenuFactory(
                NAVIGATOR_FILE_SYSTEM,
                this::onFSEntryIgnored,
                this::openInNewTab);

        this.filesTable = new FilesTable(
                NAVIGATOR_FILE_SYSTEM,
                contextMenuFactory,
                this.icons,
                frameSelection,
                this::onTableItemInvoked,
                this::onFSEntryRenamed,
                dragAndDropFiles);

        this.directoriesTree = new DirectoriesTree(
                NAVIGATOR_FILE_SYSTEM,
                this.icons,
                this.tabs,
                contextMenuFactory,
                this::onDirectorySelectedInTreeView,
                dragAndDropFiles);

        this.tabsPanel = new TabsPanel(
                this.tabs,
                this.icons,
                dragAndDropLabels,
                dragAndDropFiles,
                this::onNewTabCreated,
                this::onExistingTabSelected);

        PathBreadcrumbsBar pathBreadcrumbsBar = new PathBreadcrumbsBar(
                this.tabs,
                NAVIGATOR_FILE_SYSTEM,
                this.icons,
                this::onBreadcrumbsBarDirectorySelected,
                this.directoriesTree::selectDirectoryInCurrentTab);

        FilesView filesView = new FilesView(this.tabsPanel, this.directoriesTree, this.filesTable, pathBreadcrumbsBar);

        Region view = (Region) filesView.node();

        Group group = new Group();
        group.getChildren().addAll(view, frameSelection);
        group.setAutoSizeChildren(true);
        group.autosize();

        Scene scene = new Scene(group, 800, 600);
        scene.getStylesheets().add("file:./home/style.css");
        stage.setScene(scene);

        view.prefHeightProperty().bind(stage.getScene().heightProperty());
        view.prefWidthProperty().bind(stage.getScene().widthProperty());

        stage.show();
    }

    public void openInNewTab(String path) {
        Directory directory = NAVIGATOR_FILE_SYSTEM.toDirectory(Paths.get(path)).get();
        this.openInNewTab(directory);
    }

    public void openInNewTab(Directory directory) {
        if ( Platform.isFxApplicationThread() ) {
            this.openInNewTabInFXApplicationThread(directory);
        }
        else {
            Platform.runLater(() -> this.openInNewTabInFXApplicationThread(directory));
        }
    }

    public void openInCurrentTab(String path) {
        Directory directory = NAVIGATOR_FILE_SYSTEM.toDirectory(Paths.get(path)).get();
        if ( Platform.isFxApplicationThread() ) {
            this.openInCurrentTab(directory);
        }
        else {
            Platform.runLater(() -> this.openInCurrentTab(directory));
        }
    }

    private void openInNewTabInFXApplicationThread(Directory directory) {
        Tab tab = this.tabsPanel.newTab();
        tab.selectedDirectory().resetTo(directory);
        this.tabs.select(tab);
        this.directoriesTree.addNewTab(tab);
        this.directoriesTree.activateTabAndSelectItsDirectory(tab);
        this.filesTable.show(tab.selectedDirectory().orThrow());
    }

    private void openInCurrentTab(Directory directory) {
        Tab tab = this.tabs.selectedTabOrThrow();
        tab.selectedDirectory().resetTo(directory);
        this.directoriesTree.selectDirectoryInCurrentTab(directory);
        this.filesTable.show(tab.selectedDirectory().orThrow());
    }

    private void onTableItemInvoked(FilesTableItem tableItem) {
        FSEntry itemFsEntry = tableItem.fsEntry();
        if ( itemFsEntry.isDirectory() ) {
            Directory directory = itemFsEntry.asDirectory();
            this.directoriesTree.selectDirectoryInCurrentTab(directory);
        }
        else {
            File file = itemFsEntry.asFile();
            file.open();
        }
    }

    private void onNewTabCreated(Tab tab) {
        tab.selectedDirectory().resetTo(NAVIGATOR_FILE_SYSTEM.machineDirectory());
        this.tabs.select(tab);
        this.directoriesTree.addNewTab(tab);
        this.directoriesTree.activateTabAndSelectItsDirectory(tab);
        this.filesTable.show(tab.selectedDirectory().orThrow());
    }

    private void onExistingTabSelected(Tab tab) {
        if ( this.tabs.isNotSelected(tab) ) {
            this.tabs.select(tab);
            this.directoriesTree.activateTabAndSelectItsDirectory(tab);
            this.filesTable.show(tab.selectedDirectory().orThrow());
        }
        else {
            Possible<Directory> directorySelection = tab.selectedDirectory();

            if ( directorySelection.isPresent() ) {
                Directory selectedDirectory = directorySelection.orThrow();
                this.directoriesTree.selectDirectoryInCurrentTab(selectedDirectory);
                this.filesTable.show(selectedDirectory);
            }
            else {
                this.filesTable.clear();
            }
        }
    }

    private void onBreadcrumbsBarDirectorySelected(Directory directory, MouseEvent mouseEvent) {
        if (Platform.isFxApplicationThread() ) {
            this.onBreadcrumbsBarDirectorySelectedInFXThread(directory);
        }
        else {
            Platform.runLater(() -> this.onBreadcrumbsBarDirectorySelectedInFXThread(directory));
        }
    }

    private void onBreadcrumbsBarDirectorySelectedInFXThread(Directory directory) {
        this.tabs.selectedTabOrThrow().selectedDirectory().resetTo(directory);
        this.directoriesTree.selectDirectoryInCurrentTab(directory);
    }

    private void onDirectorySelectedInTreeView(Directory directory) {
        if (Platform.isFxApplicationThread() ) {
            this.onDirectorySelectedInTreeViewInFXThread(directory);
        }
        else {
            Platform.runLater(() -> this.onDirectorySelectedInTreeViewInFXThread(directory));
        }
    }

    private void onDirectorySelectedInTreeViewInFXThread(Directory directory) {
        this.tabs.selectedTabOrThrow().selectedDirectory().resetTo(directory);
        this.filesTable.show(directory);
    }

    private void onFSEntryIgnored(FSEntry fsEntry) {
        if ( fsEntry.canBeIgnored() ) {
            NAVIGATOR_IGNORES.ignore(fsEntry);
            this.directoriesTree.remove(fsEntry);
            this.filesTable.remove(fsEntry);
        }
    }

    private void onFSEntryRenamed(FSEntry entry, String newName) {
        NAVIGATOR_FILE_SYSTEM.rename(entry, newName);
    }
}
