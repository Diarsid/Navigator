package diarsid.navigator;

import java.util.Optional;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
import diarsid.navigator.view.tree.DirectoriesTree;
import diarsid.navigator.view.icons.Icons;
import diarsid.navigator.view.table.FileTableItem;
import diarsid.navigator.view.table.FilesTable;
import diarsid.navigator.view.dragdrop.DragAndDropContext;
import diarsid.navigator.view.tabs.LabelsAtTabs;
import diarsid.navigator.view.tabs.TabsPanel;
import diarsid.support.objects.references.real.Possible;

public class MainS extends Application {

    private Ignores ignores;
    private FS fs;
    private Tabs tabs;
    private DirectoriesAtTabs directoriesAtTabs;
    private Icons icons;
    private DirectoriesTree directoriesTree;
    private FilesTable filesTable;

    public MainS() {
        this.ignores = Ignores.INSTANCE;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.fs = FS.INSTANCE;
        this.icons = Icons.INSTANCE;
        this.tabs = new Tabs();
        this.directoriesAtTabs = new DirectoriesAtTabs();

        this.filesTable = new FilesTable(this.icons, this::onTableItemInvoked);

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
                this.filesTable::set,
                onFSEntryIgnored);

        DragAndDropContext<Label> dragAndDropContextTab = new DragAndDropContext<>("tab");
        LabelsAtTabs labelsAtTabs = new LabelsAtTabs(this::onTabSelected, dragAndDropContextTab);

        TabsPanel tabsPanel = new TabsPanel(this.tabs, this.directoriesAtTabs, labelsAtTabs, this.directoriesTree, dragAndDropContextTab);

        FilesView filesView = new FilesView(tabsPanel, directoriesTree, filesTable);

        Scene scene = new Scene((Parent) filesView.node(), 800, 600);
        scene.getStylesheets().add("file:D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/home/style.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void onTableItemInvoked(FileTableItem tableItem) {
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
            selectedDirectoryAtTab.directory().feedChildren(this.filesTable::set);
            this.directoriesTree.select(selectedDirectoryAtTab);
        }
        else {
            this.filesTable.clear();
        }
    }

}

