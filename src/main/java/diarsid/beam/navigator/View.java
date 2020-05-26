package diarsid.beam.navigator;

import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import diarsid.beam.navigator.filesystem.Directory;
import diarsid.beam.navigator.filesystem.FS;
import diarsid.beam.navigator.filesystem.FSEntry;
import diarsid.beam.navigator.filesystem.File;
import diarsid.beam.navigator.filesystem.ignoring.Ignores;
import diarsid.beam.navigator.model.DirectoriesAtTabs;
import diarsid.beam.navigator.model.DirectoryAtTab;
import diarsid.beam.navigator.model.Tab;
import diarsid.beam.navigator.model.Tabs;
import diarsid.beam.navigator.view.FilesView;
import diarsid.beam.navigator.view.tree.DirectoriesTree;
import diarsid.beam.navigator.view.icons.Icons;
import diarsid.beam.navigator.view.table.FileTableItem;
import diarsid.beam.navigator.view.table.FilesTable;
import diarsid.beam.navigator.view.dragdrop.DragAndDropContext;
import diarsid.beam.navigator.view.tabs.LabelsAtTabs;
import diarsid.beam.navigator.view.tabs.TabsPanel;
import diarsid.support.objects.references.real.Possible;

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
        DragAndDropContext<Label> dragAndDropContext = new DragAndDropContext<>("tab");
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

        LabelsAtTabs labelsAtTabs = new LabelsAtTabs(this::onTabSelected, dragAndDropContext);

        this.tabsPanel = new TabsPanel(this.tabs, this.directoriesAtTabs, labelsAtTabs, this.directoriesTree, dragAndDropContext);

        FilesView filesView = new FilesView(tabsPanel, directoriesTree, filesTable);

        Region view = (Region) filesView.node();
        Group group = new Group();
        group.getChildren().add(view);
        group.setAutoSizeChildren(true);
        group.autosize();

        Scene scene = new Scene(group, 800, 600);
        scene.getStylesheets().add("file:D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/style.css");
        stage.setScene(scene);

        view.prefHeightProperty().bind(stage.getScene().heightProperty());
        view.prefWidthProperty().bind(stage.getScene().widthProperty());

        VIEW_GROUP = group;

        stage.show();
    }

    public void open(Directory directory) {
        this.tabsPanel.newTab(true, directory);
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
