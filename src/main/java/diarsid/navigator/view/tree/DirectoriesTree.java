package diarsid.navigator.view.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.filesystem.FileSystem;
import diarsid.navigator.model.DirectoriesAtTabs;
import diarsid.navigator.model.DirectoryAtTab;
import diarsid.navigator.model.Tab;
import diarsid.navigator.view.ViewComponent;
import diarsid.navigator.view.dragdrop.DragAndDropNodes;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.objects.groups.Running;
import diarsid.support.objects.references.impl.Possible;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

import static diarsid.navigator.filesystem.Directory.Edit.MOVED;
import static diarsid.navigator.filesystem.Directory.Edit.RENAMED;
import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class DirectoriesTree implements ViewComponent {

    private final FileSystem fileSystem;
    private final Icons icons;
    private final DirectoriesAtTabs directoriesAtTabs;
    private final DirectoryAtTabTreeItems directoryAtTabTreeItems;
    private final Possible<DirectoryAtTab> selected;
    private final Consumer<DirectoryAtTab> onDirectorySelected;
    private final TreeView<String> treeView;
    private final Map<Tab, TreeItem<String>> tabsTreeRoots;
    private final Map<Directory, Running> onDirectoryChangedListeners;
    private final DragAndDropNodes<DirectoriesTreeCell> dragAndDropTreeCell;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;

    public DirectoriesTree(
            FileSystem fileSystem,
            Icons icons,
            DirectoriesAtTabs directoriesAtTabs,
            Consumer<DirectoryAtTab> onDirectoryAtTabSelected,
            Consumer<FSEntry> onIgnore,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        this.fileSystem = fileSystem;
        this.icons = icons;
        this.directoriesAtTabs = directoriesAtTabs;
        this.onDirectorySelected = onDirectoryAtTabSelected;
        this.tabsTreeRoots = new HashMap<>();
        this.onDirectoryChangedListeners = new HashMap<>();
        this.dragAndDropTreeCell = new DragAndDropNodes<>("tree-cell");
        this.dragAndDropFiles = dragAndDropFiles;

        this.directoryAtTabTreeItems = new DirectoryAtTabTreeItems(
                this.directoriesAtTabs,
                this::onTreeItemExpanded,
                this::onTreeItemCollapsed,
                this::onTreeItemCreated,
                this::onTreeItemRemoved);

        this.selected = possibleButEmpty();

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

    private void onTreeItemExpanded(DirectoryAtTabTreeItem expandedItem) {
        if ( this.selected.isPresent() ) {
            DirectoryAtTab selectedDirectoryAtTab = this.selected.orThrow();
            if ( expandedItem.directory().isParentOf(selectedDirectoryAtTab.directory()) ) {
                this.select(selectedDirectoryAtTab);
            }
        }
    }

    private void onTreeItemCollapsed(DirectoryAtTabTreeItem collapsedItem) {
        if ( this.selected.isPresent() ) {
            Directory collapsedDir = collapsedItem.directory();
            Directory selectedDir = this.selected.orThrow().directory();
            if ( collapsedDir.isParentOf(selectedDir) ) {
                this.clearTreeViewSelection();
            }
            else if ( collapsedDir.equals(selectedDir) ) {

            } else {
                this.select(this.selected.orThrow());
            }
        }
    }

    private void onTreeItemCreated(DirectoryAtTabTreeItem newItem) {
        Directory directory = newItem.directory();

        if ( directory.canNotBe(MOVED) || directory.canNotBe(RENAMED) ) {
            return;
        }

        if ( this.onDirectoryChangedListeners.containsKey(directory) ) {
            return;
        }

        Running listener = directory.listenForChanges(this::onDirectoriesTreeChanged);
        this.onDirectoryChangedListeners.put(directory, listener);
    }

    private void onTreeItemRemoved(DirectoryAtTabTreeItem removedItem) {
        Directory directory = removedItem.directory();
        Running listener = this.onDirectoryChangedListeners.remove(directory);
        if ( nonNull(listener) ) {
            listener.cancel();
        }
    }

    private void onDirectoriesTreeChanged() {
        System.out.println("LISTENING");
        this.select(this.selected.orThrow());
    }

    DirectoryAtTabTreeItem getTreeItemFor(Directory directory) {
        Tab tab = this.selected.orThrow().tab();
        DirectoryAtTab directoryAtTab = this.directoriesAtTabs.join(tab, directory);
        return this.directoryAtTabTreeItems.wrap(directoryAtTab);
    }

    public void add(Tab tab, Directory directory, boolean select) {
        TreeItem<String> tabTreeRoot = this.assignRootTreeItemToTab(tab, select);
        this.tabsTreeRoots.put(tab, tabTreeRoot);

        directory.parents().forEach(parent -> {
            DirectoryAtTab parentAtTab = this.directoriesAtTabs.join(tab, parent);
            DirectoryAtTabTreeItem parentTreeItem = this.directoryAtTabTreeItems.wrap(parentAtTab);
            expandIfNotExpanded(parentTreeItem);
        });

        DirectoryAtTab directoryAtTab = this.directoriesAtTabs.join(tab, directory);
        DirectoryAtTabTreeItem directoryTreeItem = this.directoryAtTabTreeItems.wrap(directoryAtTab);
        expandIfNotExpanded(directoryTreeItem);

        if ( select ) {
            this.treeView.setRoot(tabTreeRoot);
            this.select(directoryAtTab);
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
                        this.selectInTreeView(newSelectionItem);
                    }
                }
            }
        }
    }

    public void select(DirectoryAtTab directoryAtTab) {
        Tab tab = directoryAtTab.tab();
        Directory directory = directoryAtTab.directory();

        Directory machineDirectory = this.fileSystem.machineDirectory();
        DirectoryAtTab machineDirectoryAtTab = this.directoriesAtTabs.join(tab, machineDirectory);
        DirectoryAtTabTreeItem machineDirectoryTreeItem = this.directoryAtTabTreeItems.wrap(machineDirectoryAtTab);
        expandIfNotExpanded(machineDirectoryTreeItem);

        directory.parents().forEach(parent -> {
            DirectoryAtTab parentAtTab = this.directoriesAtTabs.join(tab, parent);
            DirectoryAtTabTreeItem parentTreeItem = this.directoryAtTabTreeItems.wrap(parentAtTab);
            expandIfNotExpanded(parentTreeItem);
        });

        DirectoryAtTabTreeItem selectedTreeItem = this.directoryAtTabTreeItems.wrap(directoryAtTab);
        expandIfNotExpanded(selectedTreeItem);

        this.selectInTreeView(selectedTreeItem);
    }

    private static void expandIfNotExpanded(TreeItem<?> treeItem) {
        if ( treeItem.isExpanded() ) {
            return;
        }

        treeItem.setExpanded(true);
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

        if ( this.selected.isPresent() ) {
            DirectoryAtTab selectedDirectoryAtTab = this.selected.orThrow();
            Directory selectedDirectory = this.selected.orThrow().directory();
            if ( directory.isParentOf(selectedDirectory) ) {
                Optional<Directory> parentOfRemoved = directory.parent();
                if ( parentOfRemoved.isPresent() ) {
                    Directory newSelection = parentOfRemoved.get();
                    Tab selectedTab = selectedDirectoryAtTab.tab();
                    DirectoryAtTab newSelectionAtTab = this.directoriesAtTabs.join(selectedTab, newSelection);
                    this.select(newSelectionAtTab);
                }
            }
        }
    }

    private void selectInTreeView(DirectoryAtTabTreeItem treeItem) {
        DirectoryAtTab directoryAtTab = treeItem.directoryAtTab();
        this.onDirectorySelected.accept(directoryAtTab);
        this.treeView.getSelectionModel().select(treeItem);
        this.selected.resetTo(treeItem.directoryAtTab());
    }

    private void clearTreeViewSelection() {
        this.treeView.getSelectionModel().clearSelection();
    }

    @Override
    public Node node() {
        return this.treeView;
    }

    private TreeItem<String> assignRootTreeItemToTab(Tab tab, boolean selectTab) {

        DirectoryAtTab machineDirectoryAtTab = this.directoriesAtTabs.join(tab, this.fileSystem.machineDirectory());
        DirectoryAtTabTreeItem machineDirectoryTreeItem = this.directoryAtTabTreeItems.wrap(machineDirectoryAtTab);

        expandIfNotExpanded(machineDirectoryTreeItem);

        TreeItem<String> network = new TreeItem<>("Network");

        DirectoriesTreeTabRoot rootTreeItem = new DirectoriesTreeTabRoot(machineDirectoryTreeItem, network);

        if ( selectTab ) {
            this.selectInTreeView(machineDirectoryTreeItem);
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
