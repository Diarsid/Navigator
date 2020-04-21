package diarsid.beam.navigator;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import diarsid.beam.navigator.filesystem.Directory;
import diarsid.beam.navigator.filesystem.FS;
import diarsid.beam.navigator.filesystem.FSEntry;
import diarsid.beam.navigator.model.DirectoryAtTab;
import diarsid.beam.navigator.model.DirectoriesAtTabs;
import diarsid.beam.navigator.model.Tab;
import diarsid.beam.navigator.view.filetree.CustomTreeCell;
import diarsid.beam.navigator.view.filetree.DirectoryAtTabTreeItem;
import diarsid.beam.navigator.view.filetree.DirectoryAtTabTreeItems;
import diarsid.beam.navigator.view.tabs.LabelAtTab;
import diarsid.beam.navigator.view.tabs.LabelsAtTabs;
import diarsid.support.objects.references.real.Possible;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import static javafx.scene.layout.Priority.ALWAYS;

public class MainS extends Application {

    public MainS() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FS fs = FS.INSTANCE;
        Directory rootDirectory = fs.rootDirectory();
        LabelsAtTabs labelsAtTabs = new LabelsAtTabs();
        DirectoriesAtTabs directoriesAtTabs = new DirectoriesAtTabs(new HashMap<>());
        BiFunction<Tab, Directory, DirectoryAtTab> joinTabAndDirectory = directoriesAtTabs::join;

        SplitPane splitPane = new SplitPane();

        Scene scene = new Scene(splitPane, 800, 600);
        scene.getStylesheets().add("file:D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/style.css");

        TableView<FSEntry> tableView = new TableView<>();

//        TableColumn<FSEntry, String> column1 = new TableColumn<>("Icon");
//        column1.setCellValueFactory(new PropertyValueFactory<>("icon"));

        TableColumn<FSEntry, String> column2 = new TableColumn<>("Name");
        column2.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<FSEntry, Integer> column3 = new TableColumn<>("Size");
        column3.setCellValueFactory(new PropertyValueFactory<>("size"));

//        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);
        tableView.getColumns().add(column3);

        tableView.setStyle("-fx-focus-color: transparent;");
        HBox.setHgrow(tableView, ALWAYS);

        Consumer<List<FSEntry>> setEntriesInTable = tableView.getItems()::setAll;

        TreeView<String> treeView = new TreeView<>();
        treeView.setStyle("-fx-background-color: lightgrey;");
        treeView.setStyle("-fx-focus-color: transparent;");
        treeView.setPrefHeight(Double.POSITIVE_INFINITY);
        treeView.setMinSize(100, 100);
        treeView.setPrefSize(100, 100);
        treeView.setShowRoot(false);
        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldItem, newItem) -> {
                    if (nonNull(newItem)) {
                        DirectoryAtTabTreeItem treeItem = (DirectoryAtTabTreeItem) newItem;
                        treeItem.directory().feedChildren(setEntriesInTable);
                        treeItem.selected();
                    }
                });

        treeView.setCellFactory((treeView1) -> {
            TreeCell<String> treeCell = new CustomTreeCell<>();

            treeCell.setOnMouseClicked((event) -> {
                DirectoryAtTabTreeItem treeItem = (DirectoryAtTabTreeItem) treeCell.getTreeItem();
                System.out.println(format("Clicked on %s:%s", treeItem.tab().name(), treeItem.directory().name()));
            });

            return treeCell;
        });

        Consumer<DirectoryAtTabTreeItem> onExpandedOrCollapsed = (treeItem) -> treeView.getSelectionModel().select(treeItem);

        DirectoryAtTabTreeItems directoryAtTabTreeItems = new DirectoryAtTabTreeItems(
                new HashMap<>(),
                joinTabAndDirectory,
                onExpandedOrCollapsed,
                onExpandedOrCollapsed);

        BiConsumer<LabelAtTab, MouseEvent> onTabSelected = (labelAtTab, mouseEvent) -> {
            treeView.setRoot(labelAtTab.tabTreeRoot());
            Possible<DirectoryAtTab> directorySelection = labelAtTab.tab().selection();

            if ( directorySelection.isPresent() ) {
                DirectoryAtTab selectedDirectoryAtTab = directorySelection.orThrow();
                DirectoryAtTabTreeItem selectedTreeItem = directoryAtTabTreeItems.wrap(selectedDirectoryAtTab);
                selectedDirectoryAtTab.directory().feedChildren(setEntriesInTable);
                treeView.getSelectionModel().select(selectedTreeItem);
            }
            else {
                tableView.getItems().clear();
            }
        };

        Function<Tab, DirectoryAtTabTreeItem> assignRootTreeItemToTab = (tab) -> {
            DirectoryAtTab rootDirectoryAtTab = directoriesAtTabs.join(tab, rootDirectory);
            DirectoryAtTabTreeItem rootTreeItem = directoryAtTabTreeItems.wrap(rootDirectoryAtTab);

            rootTreeItem.setExpanded(true);

            return rootTreeItem;
        };

        for (int i = 0; i < 4; i++) {
            Tab tab = new Tab("Tab " + (i + 1));
            labelsAtTabs.add(tab, assignRootTreeItemToTab, onTabSelected);
        }

        splitPane.getItems().addAll(labelsAtTabs.tabsPanel(), treeView, tableView);
        splitPane.setDividerPositions(0.1, 0.4, 0.9);
        SplitPane.setResizableWithParent(labelsAtTabs.tabsPanel(), false);
        SplitPane.setResizableWithParent(treeView, false);
//        hBox.getChildren().addAll(tabsPanel, separatorOne, treeView, separatorTwo, tableView);

        primaryStage.setScene(scene);
//        primaryStage.sizeToScene();
        primaryStage.show();


    }

}

