package diarsid.navigator.view.tree;

import java.util.List;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.icons.Icon;
import diarsid.navigator.view.icons.Icons;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import static diarsid.navigator.filesystem.Directory.Edit.MOVED;
import static javafx.scene.input.TransferMode.MOVE;

public class DirectoriesTreeCell extends TreeCell<String> {

    private final Icons icons;
    private final ImageView iconView;
    private final DirectoriesTree directoriesTree;

    DirectoriesTreeCell(Icons icons, DirectoriesTree directoriesTree) {
        super();

        this.icons = icons;
        this.directoriesTree = directoriesTree;
        this.getStyleClass().add("directories-tree-cell");

        this.iconView = new ImageView();
        ReadOnlyDoubleProperty size = this.icons.sizeProperty();
        this.iconView.fitWidthProperty().bind(size);
        this.iconView.fitHeightProperty().bind(size);
        this.iconView.setPreserveRatio(true);
        this.iconView.getStyleClass().add("icon");

        super.setOnDragOver(this::onDragOver);
        super.setOnDragExited(this::onDragExited);
        super.setOnDragDropped(this::onDragDropped);
        super.setOnDragDetected(this::onDragDetected);
    }

    private DirectoryAtTabTreeItem directoryAtTabTreeItem() {
        TreeItem<String> item = super.getTreeItem();

        if ( nonNull(item) && item instanceof DirectoryAtTabTreeItem ) {
            return (DirectoryAtTabTreeItem) item;
        }
        else {
            return null;
        }
    }

    @Override
    protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);

        if (empty || name == null) {
            super.setText(null);
            super.setGraphic(null);
        } else {
            TreeItem<String> item = super.getTreeItem();

            if ( nonNull(item) ) {
                if ( item instanceof DirectoryAtTabTreeItem ) {
                    DirectoryAtTabTreeItem directoryTreeItem = (DirectoryAtTabTreeItem) item;
                    Icon icon = this.icons.getFor(directoryTreeItem.directory());
                    this.iconView.setImage(icon.image());

                    super.setText(directoryTreeItem.directory().name());
                    super.setGraphic(this.iconView);
                }
                else {
                    super.setText(name);
                    super.setGraphic(null);
                }
            }
            else {
                super.setText(name);
                super.setGraphic(null);
            }
        }
    }


    private void onDragDetected(MouseEvent event) {
        if ( super.isEmpty() ) {
            return;
        }

        TreeItem<String> item = super.getTreeItem();

        if ( ! (item instanceof DirectoryAtTabTreeItem) ) {
            return;
        }

        DirectoryAtTabTreeItem directoryItem = (DirectoryAtTabTreeItem) item;

        if ( directoryItem.directory().canNotBe(MOVED) ) {
            return;
        }

        this.directoriesTree.cellDragAndDrop().setTabDraggedContextTo(this);
//        super.pseudoClassStateChanged(MOVED, true);
        event.consume();
    };

    private void onDragOver(DragEvent dragEvent) {
        if ( super.isEmpty() ) {
            return;
        }

        if ( this.directoriesTree.cellDragAndDrop().isDragOverAcceptable(dragEvent) ) {
            DirectoriesTreeCell droppedCell = (DirectoriesTreeCell) dragEvent.getGestureSource();
            DirectoryAtTabTreeItem droppedItem = droppedCell.directoryAtTabTreeItem();
            DirectoryAtTabTreeItem acceptingItem = this.directoryAtTabTreeItem();

            if ( nonNull(droppedItem) && nonNull(acceptingItem) ) {
                if ( droppedItem.equals(acceptingItem) ) {
                    System.out.println("MOVE NOT ACCEPTABLE : items are the same");
                }
                else if ( droppedItem.isParentOf(acceptingItem) ) {
                    System.out.println("MOVE NOT ACCEPTABLE : " + acceptingItem.directory() + " is child of " + droppedItem.directory());
                }
                else {
                    dragEvent.acceptTransferModes(MOVE);
                    dragEvent.consume();
//                  super.pseudoClassStateChanged(REPLACE_CANDIDATE, true);
                }
            }
        }

        if ( this.directoriesTree.dragAndDropFiles().isDragAcceptable(dragEvent) ) {
            System.out.println("dragging files into tree");
            List<FSEntry> fsEntries = this.directoriesTree.dragAndDropFiles().get();

            DirectoryAtTabTreeItem acceptingItem = this.directoryAtTabTreeItem();
            if ( isNull(acceptingItem) ) {
                return;
            }
            Directory acceptingDirectory = acceptingItem.directory();

            if ( fsEntries.size() == 1 ) {
                FSEntry fsEntry = fsEntries.get(0);
                if ( fsEntry.isFile() ) {
                    dragEvent.acceptTransferModes(MOVE);
                    dragEvent.consume();
//                  super.pseudoClassStateChanged(REPLACE_CANDIDATE, true);
                }
                else {
                    Directory droppedDirectory = fsEntry.asDirectory();

                    if ( acceptingDirectory.canHost(droppedDirectory) ) {
                        dragEvent.acceptTransferModes(MOVE);
                        dragEvent.consume();
//                      super.pseudoClassStateChanged(REPLACE_CANDIDATE, true);
                    }
                }
            }
            else if ( fsEntries.size() > 1 ) {
                boolean canNotHost = fsEntries
                        .stream()
                        .anyMatch(fsEntry -> fsEntry.isDirectory() && acceptingDirectory.canNotHost(fsEntry));

                if ( ! canNotHost ) {
                    dragEvent.acceptTransferModes(MOVE);
                    dragEvent.consume();
//                  super.pseudoClassStateChanged(REPLACE_CANDIDATE, true);
                }
            }
            else {

            }
        }
    }

    private void onDragExited(DragEvent dragEvent) {
        if ( this.directoriesTree.cellDragAndDrop().isDragOverAcceptable(dragEvent) ) {
//            super.pseudoClassStateChanged(REPLACE_CANDIDATE, false);
        }

        if ( this.directoriesTree.dragAndDropFiles().isDragAcceptable(dragEvent) ) {
//            super.pseudoClassStateChanged(REPLACE_CANDIDATE, false);
        }
    };

    private void onDragDropped(DragEvent dragEvent) {
        boolean success;

        boolean isTreeCellDropAcceptable = this.directoriesTree.cellDragAndDrop().isDropAcceptable(dragEvent);
        if ( isTreeCellDropAcceptable ) {
            try {
                DirectoriesTreeCell droppedCell = (DirectoriesTreeCell) dragEvent.getGestureSource();
                DirectoryAtTabTreeItem droppedItem = droppedCell.directoryAtTabTreeItem();
                DirectoryAtTabTreeItem acceptingItem = this.directoryAtTabTreeItem();

                if ( nonNull(droppedItem) ) {
                    if ( nonNull(acceptingItem) ) {
                        Directory droppedDirectory = droppedItem.directory();
                        Directory acceptingDirectory = acceptingItem.directory();
                        boolean moved = acceptingDirectory.host(droppedDirectory);
                        if ( moved ) {
                            acceptingItem.setExpanded(true);
                            this.directoriesTree.select(acceptingItem);
                        }
                    }
                    else {
                        System.out.println("DROP ACCEPTED - NO ACCEPTING ITEM!");
                    }
                }
                else {
                    System.out.println("DROP ACCEPTED - NO DROPPED ITEM!");
                }

//                List<Label> draggableTabLabels = supersAtTabs.tabLabels();
//                int tabInsertIndex = draggableTabLabels.indexOf(super);
//
////                draggedTab.pseudoClassStateChanged(MOVED, false);
////                super.pseudoClassStateChanged(REPLACE_CANDIDATE, false);
//
//                draggableTabLabels.remove(draggedCell);
//                draggableTabLabels.add(tabInsertIndex, draggedCell);
//
//                supersAtTabs.onTabsReorderedAction.orThrow().run();

                success = true;
            }
            catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }
        else if ( this.directoriesTree.dragAndDropFiles().isDropAcceptable(dragEvent) ) {
            List<FSEntry> fsEntries = this.directoriesTree.dragAndDropFiles().get();
            Directory acceptingDirectory = this.directoryAtTabTreeItem().directory();
            System.out.println("dropped into " + acceptingDirectory);
            fsEntries.forEach(fsEntry -> System.out.println("   -> " + fsEntry.path()));

            fsEntries.forEach(fsEntry -> acceptingDirectory.host(fsEntry));

            success = true;
        }
        else {
            success = false;
        }

        dragEvent.setDropCompleted(success);
        dragEvent.consume();
    }
}
