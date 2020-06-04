package diarsid.navigator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import diarsid.navigator.view.breadcrumbs.PathBreadcrumbsBar;
import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FS;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.filesystem.File;
import diarsid.navigator.filesystem.ignoring.Ignores;
import diarsid.navigator.model.DirectoriesAtTabs;
import diarsid.navigator.model.DirectoryAtTab;
import diarsid.navigator.model.Tab;
import diarsid.navigator.model.Tabs;
import diarsid.navigator.view.FilesView;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.navigator.view.tree.DirectoriesTree;
import diarsid.navigator.view.icons.Icons;
import diarsid.navigator.view.table.FilesTableItem;
import diarsid.navigator.view.table.FilesTable;
import diarsid.navigator.view.dragdrop.DragAndDropNodes;
import diarsid.navigator.view.tabs.LabelsAtTabs;
import diarsid.navigator.view.tabs.TabsPanel;
import diarsid.navigator.view.tree.DirectoriesTreeCell;
import diarsid.support.objects.references.impl.Possible;

import static javafx.stage.StageStyle.DECORATED;

public class View {

    public static Group VIEW_GROUP;

    private Ignores ignores;
    private FS fs;
    private Tabs tabs;
    private DirectoriesAtTabs directoriesAtTabs;
    private Icons icons;
    private DirectoriesTree directoriesTree;
    private FilesTable filesTable;
    private TabsPanel tabsPanel;

    public View() {
        Stage stage = new Stage();
        stage.initStyle(DECORATED);

        this.ignores = Ignores.INSTANCE;
        DragAndDropNodes<Label> dragAndDropLabels = new DragAndDropNodes<>("tab");
        this.fs = FS.INSTANCE;
        this.icons = Icons.INSTANCE;
        this.tabs = new Tabs();
        this.directoriesAtTabs = new DirectoriesAtTabs();

        Map<Class<? extends Node>, String> classes = new HashMap<>();
        classes.put(Label.class, "label-at-tab");
        classes.put(DirectoriesTreeCell.class, "directories-tree-cell");
        DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles = new DragAndDropObjectTransfer<>(
                "drag-and-drop-files",
                classes);

        this.filesTable = new FilesTable(this.icons, this::onTableItemInvoked, dragAndDropFiles);

        Consumer<FSEntry> onFSEntryIgnored = (fsEntry) -> {
            if ( fsEntry.canBeIgnored() ) {
                this.ignores.ignore(fsEntry);
                this.directoriesTree.remove(fsEntry);
                this.filesTable.remove(fsEntry);
            }
        };

        this.directoriesTree = new DirectoriesTree(
                this.fs,
                this.icons,
                this.directoriesAtTabs,
                this::onDirectoryAtTabSelected,
                onFSEntryIgnored,
                dragAndDropFiles);

        LabelsAtTabs labelsAtTabs = new LabelsAtTabs(this::onTabSelected, dragAndDropLabels, dragAndDropFiles);

        this.tabsPanel = new TabsPanel(
                this.tabs, this.directoriesAtTabs, labelsAtTabs, this.directoriesTree, dragAndDropLabels);

        PathBreadcrumbsBar pathBreadcrumbsBar = new PathBreadcrumbsBar(tabs, fs, icons);
        pathBreadcrumbsBar.size().bindTo(icons.size());

        FilesView filesView = new FilesView(tabsPanel, directoriesTree, filesTable, pathBreadcrumbsBar);

        Region view = (Region) filesView.node();
        Group group = new Group();
        group.getChildren().add(view);
        group.setAutoSizeChildren(true);
        group.autosize();

        Scene scene = new Scene(group, 800, 600);
        scene.getStylesheets().add("file:./home/style.css");
        stage.setScene(scene);

        view.prefHeightProperty().bind(stage.getScene().heightProperty());
        view.prefWidthProperty().bind(stage.getScene().widthProperty());

        VIEW_GROUP = group;

        stage.show();
    }

    public void open(Directory directory) {
        this.tabsPanel.newTab(true, directory);
    }

    private void onTableItemInvoked(FilesTableItem tableItem) {
        FSEntry itemFsEntry = tableItem.fsEntry();
        if ( itemFsEntry.isDirectory() ) {
            Optional<Directory> parentDirectory = itemFsEntry.parent();
            if ( parentDirectory.isPresent() ) {
                Possible<Tab> selectedTab = this.tabs.selected();
                if ( selectedTab.isPresent() ) {
                    Tab tab = selectedTab.orThrow();
                    Directory directory = itemFsEntry.asDirectory();
                    this.directoriesTree.selectAndExpandParent(tab, parentDirectory.get(), directory);
                }
            }
            else {
                Directory directory = itemFsEntry.asDirectory();
                Possible<Tab> selectedTab = this.tabs.selected();
                if ( selectedTab.isPresent() ) {
                    Tab tab = selectedTab.orThrow();
                    Directory machineDirectory = this.fs.machineDirectory();
                    this.directoriesTree.selectAndExpandParent(tab, machineDirectory, directory);
                }
            }
        }
        else {
            File file = itemFsEntry.asFile();
            file.open();
        }
    }

    private void onTabSelected(Tab tab) {
        this.tabs.select(tab);
        this.directoriesTree.setActive(tab);
        Possible<DirectoryAtTab> directorySelection = tab.selectedDirectory();

        if ( directorySelection.isPresent() ) {
            DirectoryAtTab selectedDirectoryAtTab = directorySelection.orThrow();
            this.filesTable.show(selectedDirectoryAtTab.directory());
            this.directoriesTree.select(selectedDirectoryAtTab);
        }
        else {
            this.filesTable.clear();
        }
    }

    private void onDirectoryAtTabSelected(DirectoryAtTab directoryAtTab) {
        this.filesTable.show(directoryAtTab.directory());
        directoryAtTab.tab().selectedDirectory().resetTo(directoryAtTab);
    }
}
