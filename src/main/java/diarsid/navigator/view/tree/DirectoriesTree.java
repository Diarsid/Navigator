package diarsid.navigator.view.tree;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.filesystem.FileSystem;
import diarsid.navigator.model.Tab;
import diarsid.navigator.model.Tabs;
import diarsid.navigator.view.ViewComponent;
import diarsid.navigator.view.dragdrop.DragAndDropNodes;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.navigator.view.fsentry.contextmenu.FSEntryContextMenuFactory;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.objects.references.Possible;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

import static diarsid.support.concurrency.ThreadUtils.currentThreadTrack;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;

public class DirectoriesTree implements ViewComponent {

    private final FileSystem fileSystem;
    private final Icons icons;
    private final Tabs tabs;
    private final Possible<Tab> selectedTab;
    private final Possible<Directory> selectedDirectory;
    private final Consumer<Directory> onDirectorySelected;
    private final TreeView<String> treeView;
    private final Map<Tab, TreeItem<String>> tabsTreeRoots;
    private final DragAndDropNodes<DirectoriesTreeCell> dragAndDropTreeCell;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;
    private final Object treeLock;

    public DirectoriesTree(
            FileSystem fileSystem,
            Icons icons,
            Tabs tabs,
            FSEntryContextMenuFactory fsEntryContextMenuFactory,
            Consumer<Directory> onDirectorySelected,
            Consumer<FSEntry> onIgnore,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        this.fileSystem = fileSystem;
        this.icons = icons;
        this.tabs = tabs;
        this.selectedTab = simplePossibleButEmpty();
        this.selectedDirectory = simplePossibleButEmpty();
        this.onDirectorySelected = onDirectorySelected;
        this.tabsTreeRoots = new HashMap<>();
        this.dragAndDropTreeCell = new DragAndDropNodes<>("tree-cell");
        this.dragAndDropFiles = dragAndDropFiles;

        this.treeView = new TreeView<>();
        this.treeView.setPrefHeight(POSITIVE_INFINITY);
        this.treeView.setMinSize(100, 100);
        this.treeView.setPrefSize(100, 100);
        this.treeView.setShowRoot(false);

        this.fileSystem.changes().listenForEntriesAdded(this::addAll);
        this.fileSystem.changes().listenForEntriesRemoved(this::removeAll);

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
            TreeCell<String> treeCell = new DirectoriesTreeCell(this.icons, fsEntryContextMenuFactory, this);

//            treeCell.setOnMouseClicked((event) -> {
//                TreeItem<String> item = treeCell.getTreeItem();
//
//                if ( nonNull(item) ) {
//                    if ( item instanceof DirectoriesTreeItem) {
//                        DirectoriesTreeItem treeItem = (DirectoriesTreeItem) item;
//                    }
//                }
//                else {
//
//                }
//
////                event.consume();
//            });

            return treeCell;
        });

        this.treeLock = new Object();
    }

    private void addAll(List<FSEntry> fsEntries) {
        synchronized ( this.treeLock ) {
            this.treeView.getSelectionModel().clearSelection();

            fsEntries.stream()
                    .filter(FSEntry::isDirectory)
                    .map(FSEntry::asDirectory)
                    .forEach(this::addInternally);

            this.selectedDirectory.ifPresent(this::selectInternally);
        }
    }

    private void removeAll(List<Path> paths) {
        synchronized ( this.treeLock ) {
            this.treeView.getSelectionModel().clearSelection();

            paths.forEach(this::removeInternally);

            this.selectedDirectory.ifPresent(this::selectInternally);
        }
    }

    private void onTreeItemExpanded(DirectoriesTreeItem expandedItem) {
        if ( this.selectedDirectory.isPresent() ) {
            Directory selectedDirectory = this.selectedDirectory.orThrow();
            System.out.println("EXPANDED " + expandedItem.directory().path());
            // 28.05.2021 uncomment
            if ( expandedItem.directory().isIndirectParentOf(selectedDirectory) ) {
                this.selectInternally(selectedDirectory);
            }
//            this.selectInternally(selectedDirectory);
        }
    }

    private void onTreeItemCollapsed(DirectoriesTreeItem collapsedItem) {
        System.out.println("COLLAPSED " + collapsedItem.directory().path());
        if ( this.selectedTab.isPresent() ) {
            Directory collapsedDir = collapsedItem.directory();
            Directory selectedDir = this.selectedDirectory.orThrow();
            if ( collapsedDir.isIndirectParentOf(selectedDir) ) {
                this.treeView.getSelectionModel().clearSelection();
            }
            else if ( collapsedDir.equals(selectedDir) ) {

            } else {
                TreeItem<String> item = this.treeView.getSelectionModel().getSelectedItem();
                if ( item.isExpanded() ) {
                    this.selectInternally(this.selectedDirectory.orThrow());
                }
            }
        }
    }

//    private void onTreeItemCreated(DirectoryAtTabTreeItem newItem) {
//        Directory directory = newItem.directory();
//
//        if ( directory.canNotBe(MOVED) || directory.canNotBe(RENAMED) ) {
//            return;
//        }
//
//        if ( this.onDirectoryChangedListeners.containsKey(directory) ) {
//            return;
//        }
//
//        Running listener = directory.listenForChanges(this::onDirectoriesTreeChanged);
//        this.onDirectoryChangedListeners.put(directory, listener);
//    }

//    private void onTreeItemRemoved(DirectoryAtTabTreeItem removedItem) {
//        Directory directory = removedItem.directory();
//        Running listener = this.onDirectoryChangedListeners.remove(directory);
//        if ( nonNull(listener) ) {
//            listener.cancel();
//        }
//    }

//    private void onDirectoriesTreeChanged() {
//        System.out.println("LISTENING");
//        this.selectInternally(this.tabs.selected().orThrow().selectedDirectory().orThrow());
//    }

//    DirectoriesTreeItem getTreeItemFor(Directory directory) {
//        Tab tab = this.selected.orThrow().tab();
//        DirectoryAtTab directoryAtTab = this.directoriesAtTabs.join(tab, directory);
//        return this.directoryAtTabTreeItems.wrap(directoryAtTab);
//    }

    public void add(Tab tab, boolean select) {
        this.assignRootTreeItemToTab(tab);

        if ( select ) {
            this.setActive(tab);
        }
    }

    public void setActive(Tab tab) {
        if ( this.selectedTab.equalsTo(tab) ) {
            return;
        }

        TreeItem<String> tabTreeRoot = this.tabsTreeRoots.get(tab);

        if ( isNull(tabTreeRoot) ) {
            throw new IllegalArgumentException("Unknown tab");
        }

        this.selectedTab.resetTo(tab);
        Directory tabDirectory = tab.selectedDirectory().orThrow();
        this.selectedDirectory.resetTo(tabDirectory);

        this.treeView.setRoot(tabTreeRoot);
        this.select(tabDirectory);
    }

//    public void selectAndExpandParent(Tab tab, Directory parentDirectory, Directory directory) {
//        Optional<DirectoryAtTab> parentDirectoryAtTab = this.directoriesAtTabs.getBy(tab, parentDirectory);
//        if ( parentDirectoryAtTab.isPresent() ) {
//            Optional<DirectoriesTreeItem> possibleParentTreeItem = this.directoryAtTabTreeItems
//                    .getExistedBy(parentDirectoryAtTab.get());
//            if ( possibleParentTreeItem.isPresent() ) {
//                DirectoriesTreeItem parentTreeItem = possibleParentTreeItem.get();
//                parentTreeItem.setExpanded(true);
//
//                Optional<DirectoryAtTab> targetAtTab = this.directoriesAtTabs.getBy(tab, directory);
//                if ( targetAtTab.isPresent() ) {
//                    Optional<DirectoriesTreeItem> newSelection = this.directoryAtTabTreeItems.getExistedBy(targetAtTab.get());
//                    if ( newSelection.isPresent() ) {
//                        DirectoriesTreeItem newSelectionItem = newSelection.get();
//                        newSelectionItem.setExpanded(true);
//                        this.selectInTreeView(newSelectionItem);
//                    }
//                }
//            }
//        }
//    }

    public void select(Directory directory) {
//        if ( this.selectedDirectory.equalsTo(directory) ) {
//            return;
//        }

        this.selectInternally(directory);
    }

    private void selectInternally(Directory directory) {
        if ( directory.isAbsent() ) {
            directory = directory.existedParent().orElse(this.fileSystem.machineDirectory());
        }
        Directory oldDirectory = this.selectedDirectory.resetTo(directory);
        boolean same = nonNull(oldDirectory) && oldDirectory.equals(directory);
        DirectoriesTreeItem machineItem = this.getMachineItemFromSelectedRoot();

        DirectoriesTreeItem prevParentItem = machineItem;
        DirectoriesTreeItem parentItem = null;
        for ( Directory parent : directory.parents() ) {
            expandIfNotExpanded(prevParentItem);
            parentItem = prevParentItem.getInChildrenOrCreate(parent);
            prevParentItem = parentItem;
        }

        DirectoriesTreeItem directoryItem;
        if ( nonNull(parentItem) ) {
            directoryItem = parentItem.getInChildrenOrCreate(directory);
        }
        else {
            if ( directory.equals(this.fileSystem.machineDirectory()) ) {
                directoryItem = machineItem;
            }
            else if ( prevParentItem.directory().equals(machineItem.directory()) ) {
                directoryItem = machineItem.getInChildrenOrCreate(directory);
            }
            else {
                throw new IllegalStateException();
            }
        }

//        expandIfNotExpanded(directoryItem); // 28.05.2021 commented

        System.out.println("[TREE] [SELECT] " + directoryItem.getValue());
//        currentThreadTrack("diarsid", (element) -> System.out.println("    " + element));
        if ( ! same ) {
            this.onDirectorySelected.accept(directory);
        }
        this.treeView.getSelectionModel().select(directoryItem);
    }

    private DirectoriesTreeItem getMachineItemFromSelectedRoot() {
        TreeItem<String> root = this.treeView.getRoot();
        return this.getMachineItemAssignedTo(root);
    }

    private DirectoriesTreeItem getMachineItemAssignedTo(TreeItem<String> root) {
        Directory machineDirectory = this.fileSystem.machineDirectory();

        DirectoriesTreeItem machineDirectoryItem = null;

        for ( TreeItem<String> rootChild : root.getChildren() ) {
            if ( rootChild.getValue().equalsIgnoreCase(machineDirectory.name()) ) {
                machineDirectoryItem = (DirectoriesTreeItem) rootChild;
            }
        }
        if ( isNull(machineDirectoryItem) ) {
            throw new IllegalStateException();
        }

        return machineDirectoryItem;
    }

//    private DirectoriesTreeItem getMachineItem() {
//        Directory machineDirectory = this.fileSystem.machineDirectory();
//        TreeItem<String> root = this.treeView.getRoot();
//
//        DirectoriesTreeItem machineDirectoryItem = null;
//
//        for ( TreeItem<String> rootChild : root.getChildren() ) {
//            if ( rootChild.getValue().equalsIgnoreCase(machineDirectory.name()) ) {
//                machineDirectoryItem = (DirectoriesTreeItem) rootChild;
//            }
//        }
//        if ( isNull(machineDirectoryItem) ) {
//            throw new IllegalStateException();
//        }
//
//        return machineDirectoryItem;
//    }

    private static void expandIfNotExpanded(TreeItem<?> treeItem) {
        if ( treeItem.isExpanded() ) {
            return;
        }

        if ( treeItem.isLeaf() ) {
            return;
        }

        treeItem.setExpanded(true);
    }

    private void addInternally(Directory directory) {
        System.out.println("[TREE] add " + directory.path());
        for ( TreeItem<String> root : this.tabsTreeRoots.values() ) {
            DirectoriesTreeItem machineItem = this.getMachineItemAssignedTo(root);

            DirectoriesTreeItem prevParentItem = machineItem;
            DirectoriesTreeItem parentItem = null;
            for ( Directory parent : directory.parents() ) {
                expandIfNotExpanded(prevParentItem);
                parentItem = prevParentItem.getInChildrenOrCreate(parent);
                prevParentItem = parentItem;
            }

            if ( isNull(parentItem) ) {
                throw new IllegalStateException();
            }

            parentItem.getInChildrenOrCreate(directory);
//            expandIfNotExpanded(parentItem); // 28.05.2021 commented
        }
    }

    private void removeInternally(Path path) {
        System.out.println("[TREE] remove " + path);
        currentThreadTrack("diarsid", (element) -> System.out.println("    " + element));
        for ( TreeItem<String> root : this.tabsTreeRoots.values() ) {
            DirectoriesTreeItem machineItem = this.getMachineItemAssignedTo(root);

            DirectoriesTreeItem prevParentItem = machineItem;
            DirectoriesTreeItem parentItem = null;
            for ( Directory parent : this.fileSystem.parentsOf(path) ) {
                parentItem = prevParentItem.getInChildrenOrNull(parent);
                if ( isNull(parentItem) ) {
                    continue;
                }
                prevParentItem = parentItem;
            }

            if ( isNull(parentItem) ) {
                continue;
            }

            boolean foundAndRemoved = parentItem.removeInChildren(path.getFileName().normalize().toString());

            if ( ! foundAndRemoved ) {
                continue;
            }

//            if ( this.selectedDirectory.isPresent() ) {
//                Directory selectedDirectory = this.selectedDirectory.orThrow();
//                Directory newSelection = selectedDirectory;
//
//                if ( selectedDirectory.has(path) || selectedDirectory.isDescendantOf(path) ) {
//                    newSelection = this.fileSystem.existedParentOf(path).orElseGet(this.fileSystem::machineDirectory);
//                }
//
//                this.selectInternally(newSelection);
//            }
        }
    }

    public void remove(FSEntry fsEntry) {
        if ( fsEntry.isFile() ) {
            return;
        }

        Directory directory = fsEntry.asDirectory();

        for ( TreeItem<String> root : this.tabsTreeRoots.values() ) {
            DirectoriesTreeItem machineItem = this.getMachineItemAssignedTo(root);

            DirectoriesTreeItem prevParentItem = machineItem;
            DirectoriesTreeItem parentItem = null;
            for ( Directory parent : directory.parents() ) {
                parentItem = prevParentItem.getInChildrenOrNull(parent);
                if ( isNull(parentItem) ) {
                    continue;
                }
                prevParentItem = parentItem;
            }

            if ( isNull(parentItem) ) {
                continue;
            }

            boolean foundAndRemoved = parentItem.removeInChildren(directory);

            if ( ! foundAndRemoved ) {
                continue;
            }

            if ( this.selectedDirectory.isPresent() ) {
                Directory selectedDirectory = this.selectedDirectory.orThrow();
                Directory newSelection = selectedDirectory;

                if ( selectedDirectory.equals(directory) || directory.isParentOf(selectedDirectory) ) {
                    newSelection = directory.existedParent().orElse(this.fileSystem.machineDirectory());
                }

                this.selectInternally(newSelection);
            }
        }
    }

    @Override
    public Node node() {
        return this.treeView;
    }

    private TreeItem<String> assignRootTreeItemToTab(Tab tab) {
        Directory machineDirectory = this.fileSystem.machineDirectory();
        DirectoriesTreeItem machineDirectoryTreeItem = new DirectoriesTreeItem(tab, machineDirectory, this::onTreeItemExpanded, this::onTreeItemCollapsed);

        TreeItem<String> network = new TreeItem<>("Network");

        DirectoriesTreeTabRoot rootTreeItem = new DirectoriesTreeTabRoot(machineDirectoryTreeItem, network);
        this.tabsTreeRoots.put(tab, rootTreeItem);

        return rootTreeItem;
    }

    DragAndDropNodes<DirectoriesTreeCell> cellDragAndDrop() {
        return this.dragAndDropTreeCell;
    }

    DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles() {
        return this.dragAndDropFiles;
    }
}
