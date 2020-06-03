package diarsid.navigator.view.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FS;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.model.DirectoriesAtTabs;
import diarsid.navigator.model.DirectoryAtTab;
import diarsid.navigator.model.Tab;
import diarsid.navigator.view.ViewComponent;
import diarsid.navigator.view.dragdrop.DragAndDropNodes;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.navigator.view.icons.Icons;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

public class DirectoriesTree implements ViewComponent {

    private final FS fs;
    private final Icons icons;
    private final DirectoriesAtTabs directoriesAtTabs;
    private final DirectoryAtTabTreeItems directoryAtTabTreeItems;
    private final Consumer<Directory> onDirectorySelected;
    private final TreeView<String> treeView;
    private final Map<Tab, TreeItem<String>> tabsTreeRoots;
    private final DragAndDropNodes<DirectoriesTreeCell> dragAndDropTreeCell;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;

    public DirectoriesTree(
            FS fs,
            Icons icons,
            DirectoriesAtTabs directoriesAtTabs,
            Consumer<Directory> onDirectorySelected,
            Consumer<FSEntry> onIgnore,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        this.fs = fs;
        this.icons = icons;
        this.directoriesAtTabs = directoriesAtTabs;
        this.onDirectorySelected = onDirectorySelected;
        this.tabsTreeRoots = new HashMap<>();
        this.dragAndDropTreeCell = new DragAndDropNodes<>("tree-cell");
        this.dragAndDropFiles = dragAndDropFiles;

        this.directoryAtTabTreeItems = new DirectoryAtTabTreeItems(
                this.directoriesAtTabs,
                this::onTreeItemExpanded,
                this::onTreeItemCollapsed);

        this.treeView = new TreeView<>();
        this.treeView.setPrefHeight(POSITIVE_INFINITY);
        this.treeView.setMinSize(100, 100);
        this.treeView.setPrefSize(100, 100);
        this.treeView.setShowRoot(false);

        this.treeView.addEventFilter(MOUSE_PRESSED, event -> {
            if ( event.isSecondaryButtonDown() ) {
                Node target = (Node) event.getTarget();
                if ( target instanceof TreeCell ) {
                    DirectoriesTreeCell treeCell = (DirectoriesTreeCell) target;
                    event.consume();
                }
                else {
                    Node parent = target.getParent();
                    if ( parent instanceof TreeCell ) {
                        DirectoriesTreeCell treeCell = (DirectoriesTreeCell) parent;
                        event.consume();
                    }
                }
            }
        });

        this.treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener(this::onTreeItemSelected);

        this.treeView.setCellFactory((tree) -> {
            TreeCell<String> treeCell = new DirectoriesTreeCell(icons, this);

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().add(new ContextMenuElementIgnore(treeCell, onIgnore));
            treeCell.setContextMenu(contextMenu);

            treeCell.setOnMouseClicked((event) -> {
                TreeItem<String> item = treeCell.getTreeItem();

                if ( nonNull(item) ) {
                    if ( item instanceof DirectoryAtTabTreeItem ) {
                        DirectoryAtTabTreeItem treeItem = (DirectoryAtTabTreeItem) item;
                    }
                }
                else {

                }

                event.consume();
            });

            return treeCell;
        });
    }

    private void onTreeItemExpanded(DirectoryAtTabTreeItem treeItem) {
        this.select(treeItem);
    }

    private void onTreeItemCollapsed(DirectoryAtTabTreeItem treeItem) {
        this.select(treeItem);
    }

    private void onTreeItemSelected(
            ObservableValue<? extends TreeItem<String>> observable,
            TreeItem<String> oldItem,
            TreeItem<String> newItem) {
        if ( nonNull(newItem) ) {
            if ( newItem instanceof DirectoryAtTabTreeItem ) {
                DirectoryAtTabTreeItem treeItem = (DirectoryAtTabTreeItem) newItem;
                Directory directory = treeItem.directory();
                this.onDirectorySelected.accept(directory);
                treeItem.setSelectedToTab();
            }
            else {

            }
        }
    }

    public void add(Tab tab, Directory directory, boolean select) {
        TreeItem<String> tabTreeRoot = this.assignRootTreeItemToTab(tab, select);
        this.tabsTreeRoots.put(tab, tabTreeRoot);

        directory.parents().forEach(parent -> {
            DirectoryAtTab parentAtTab = this.directoriesAtTabs.join(tab, parent);
            DirectoryAtTabTreeItem parentTreeItem = this.directoryAtTabTreeItems.wrap(parentAtTab);
            parentTreeItem.setExpanded(true);
        });

        DirectoryAtTab directoryAtTab = this.directoriesAtTabs.join(tab, directory);
        DirectoryAtTabTreeItem directoryTreeItem = this.directoryAtTabTreeItems.wrap(directoryAtTab);
        directoryTreeItem.setExpanded(true);

        if ( select ) {
            this.treeView.setRoot(tabTreeRoot);
        }
    }

    public void add(Tab tab, boolean select) {
        TreeItem<String> tabTreeRoot = this.assignRootTreeItemToTab(tab, select);
        this.tabsTreeRoots.put(tab, tabTreeRoot);

        if ( select ) {
            this.treeView.setRoot(tabTreeRoot);
        }
    }

    public void setActive(Tab tab) {
        TreeItem<String> tabTreeRoot = this.tabsTreeRoots.get(tab);

        if ( isNull(tabTreeRoot) ) {
            throw new IllegalArgumentException("Unkown tab");
        }

        this.treeView.setRoot(tabTreeRoot);
    }

    public void selectAndExpandParent(Tab tab, Directory parentDirectory, Directory directory) {
        Optional<DirectoryAtTab> parentDirectoryAtTab = this.directoriesAtTabs.getBy(tab, parentDirectory);
        if ( parentDirectoryAtTab.isPresent() ) {
            Optional<DirectoryAtTabTreeItem> possibleParentTreeItem = this.directoryAtTabTreeItems
                    .getExistedBy(parentDirectoryAtTab.get());
            if ( possibleParentTreeItem.isPresent() ) {
                DirectoryAtTabTreeItem parentTreeItem = possibleParentTreeItem.get();
                parentTreeItem.setExpanded(true);

                Optional<DirectoryAtTab> targetAtTab = this.directoriesAtTabs.getBy(tab, directory);
                if ( targetAtTab.isPresent() ) {
                    Optional<DirectoryAtTabTreeItem> newSelection = this.directoryAtTabTreeItems.getExistedBy(targetAtTab.get());
                    if ( newSelection.isPresent() ) {
                        DirectoryAtTabTreeItem newSelectionItem = newSelection.get();
                        newSelectionItem.setExpanded(true);
                        this.select(newSelectionItem);
                    }
                }
            }
        }
    }

    public void select(DirectoryAtTab directoryAtTab) {
        DirectoryAtTabTreeItem selectedTreeItem = this.directoryAtTabTreeItems.wrap(directoryAtTab);
        this.select(selectedTreeItem);
    }

    public void remove(FSEntry fsEntry) {
        if ( fsEntry.isFile() ) {
            return;
        }

        Directory directory = fsEntry.asDirectory();

        List<DirectoryAtTabTreeItem> directoryAtTabTreeItemsToRemove = this.directoryAtTabTreeItems.remove(directory);

        directoryAtTabTreeItemsToRemove.forEach(treeItem -> {
            TreeItem<String> parent = treeItem.getParent();
            if ( nonNull(parent) ) {
                parent.getChildren().remove(treeItem);
            }
        });
    }

    void select(DirectoryAtTabTreeItem treeItem) {
        this.treeView.getSelectionModel().select(treeItem);
    }

    @Override
    public Node node() {
        return this.treeView;
    }

    private TreeItem<String> assignRootTreeItemToTab(Tab tab, boolean selectTab) {

        DirectoryAtTab machineDirectoryAtTab = this.directoriesAtTabs.join(tab, this.fs.machineDirectory());
        DirectoryAtTabTreeItem machineDirectoryTreeItem = this.directoryAtTabTreeItems.wrap(machineDirectoryAtTab);

        machineDirectoryTreeItem.setExpanded(true);

        TreeItem<String> network = new TreeItem<>("Network");

        DirectoriesTreeTabRoot rootTreeItem = new DirectoriesTreeTabRoot(machineDirectoryTreeItem, network);

        if ( selectTab ) {
            this.select(machineDirectoryTreeItem);
        }

        return rootTreeItem;
    }

    DragAndDropNodes<DirectoriesTreeCell> cellDragAndDrop() {
        return this.dragAndDropTreeCell;
    }

    DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles() {
        return this.dragAndDropFiles;
    }
}
