package diarsid.navigator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.filesystem.File;
import diarsid.navigator.filesystem.FileSystem;
import diarsid.navigator.filesystem.ignoring.Ignores;
import diarsid.navigator.model.Identity;
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
import diarsid.support.javafx.FrameSelection;
import diarsid.support.objects.references.Possible;

import static javafx.stage.StageStyle.DECORATED;

class NavigatorView {

    private Ignores ignores;
    private FileSystem fileSystem;
    private Tabs tabs;
    private Icons icons;
    private DirectoriesTree directoriesTree;
    private FilesTable filesTable;
    private TabsPanel tabsPanel;

    NavigatorView() {
        Stage stage = new Stage();
        stage.initStyle(DECORATED);

        this.ignores = Ignores.INSTANCE;
        DragAndDropNodes<Label> dragAndDropLabels = new DragAndDropNodes<>("tab");
        this.fileSystem = FileSystem.INSTANCE;
        this.icons = Icons.INSTANCE;
        this.tabs = new Tabs();

        Map<Class<? extends Node>, String> classes = new HashMap<>();
        classes.put(Label.class, "label-at-tab");
        classes.put(DirectoriesTreeCell.class, "directories-tree-cell");
        DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles = new DragAndDropObjectTransfer<>(
                "drag-and-drop-files",
                classes);

        FrameSelection frameSelection = new FrameSelection();

        Consumer<FSEntry> onFSEntryIgnored = (fsEntry) -> {
            if ( fsEntry.canBeIgnored() ) {
                this.ignores.ignore(fsEntry);
                this.directoriesTree.remove(fsEntry);
                this.filesTable.remove(fsEntry);
            }
        };

        FSEntryContextMenuFactory contextMenuFactory = new FSEntryContextMenuFactory(fileSystem, onFSEntryIgnored);

        BiConsumer<FSEntry, String> onRename = (entry, newName) -> {
            this.fileSystem.rename(entry, newName);
        };

        this.filesTable = new FilesTable(this.fileSystem, contextMenuFactory, this.icons, frameSelection, this::onTableItemInvoked, onRename, dragAndDropFiles);

        this.directoriesTree = new DirectoriesTree(
                this.fileSystem,
                this.icons,
                this.tabs,
                contextMenuFactory,
                this::onDirectorySelectedInTreeView,
                onFSEntryIgnored,
                dragAndDropFiles);

        this.tabsPanel = new TabsPanel(
                this.tabs,
                this.directoriesTree,
                dragAndDropLabels,
                dragAndDropFiles,
                this::onTabCreated,
                this::onTabSelected);

        PathBreadcrumbsBar pathBreadcrumbsBar = new PathBreadcrumbsBar(
                this.tabs,
                this.fileSystem,
                this.icons,
                this::onBreadcrumbsBarDirectorySelected,
                this.directoriesTree::select);
        pathBreadcrumbsBar.iconsSize().bindTo(this.icons.size());

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

    public void openInNewTab(Directory directory) {
        Tab tab = this.tabsPanel.newTab();
        tab.selectedDirectory().resetTo(directory);
        this.tabs.select(tab);
        this.directoriesTree.add(tab, true);
    }

    public void openInCurrentTab(Directory directory) {
        Tab tab = this.tabs.selected().orThrow();
        tab.selectedDirectory().resetTo(directory);
        this.directoriesTree.select(directory);
    }

    private void onTableItemInvoked(FilesTableItem tableItem) {
        FSEntry itemFsEntry = tableItem.fsEntry();
        if ( itemFsEntry.isDirectory() ) {
            Directory directory = itemFsEntry.asDirectory();
            this.directoriesTree.select(directory);
        }
        else {
            File file = itemFsEntry.asFile();
            file.open();
        }
    }

    private void onTabCreated(Tab tab) {
        tab.selectedDirectory().resetTo(fileSystem.machineDirectory());
        this.tabs.select(tab);
        this.directoriesTree.add(tab, true);
        this.onTabSelected(tab);
    }

    private void onTabSelected(Tab tab) {
        if ( this.tabs.selected().isNotPresent() || this.tabs.selected().notEqualsTo(tab) ) {
            this.tabs.select(tab);
            this.directoriesTree.setActive(tab);
        }

//        this.directoriesTree.setActive(tab);
        Possible<Directory> directorySelection = tab.selectedDirectory();

        if ( directorySelection.isPresent() ) {
            Directory selectedDirectory = directorySelection.orThrow();
            this.directoriesTree.select(selectedDirectory);
            this.filesTable.show(selectedDirectory);
        }
        else {
            this.filesTable.clear();
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
        this.tabs.selected().orThrow().selectedDirectory().resetTo(directory);
        this.directoriesTree.select(directory);
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
        this.tabs.selected().orThrow().selectedDirectory().resetTo(directory);
        this.filesTable.show(directory);
    }
}
