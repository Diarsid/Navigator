package diarsid.navigator.view.tree;

import java.util.List;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import diarsid.filesystem.api.Directory;
import diarsid.filesystem.api.FSEntry;
import diarsid.filesystem.api.ProgressTracker;
import diarsid.navigator.view.fsentry.contextmenu.FSEntryContextMenu;
import diarsid.navigator.view.fsentry.contextmenu.FSEntryContextMenuFactory;
import diarsid.navigator.view.icons.Icon;
import diarsid.navigator.view.icons.Icons;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.TransferMode.MOVE;

import static diarsid.filesystem.api.Directory.Edit.MOVED;

public class DirectoriesTreeCell extends TreeCell<String> {

    private final Icons icons;
    private final ImageView iconView;
    private final DirectoriesTree directoriesTree;

    DirectoriesTreeCell(Icons icons, FSEntryContextMenuFactory contextMenuFactory, DirectoriesTree directoriesTree) {
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

        super.setOnMousePressed(this::onMousePressed);
        super.setOnDragOver(this::onDragOver);
        super.setOnDragExited(this::onDragExited);
        super.setOnDragDropped(this::onDragDropped);
        super.setOnDragDetected(this::onDragDetected);

        FSEntryContextMenu contextMenu = contextMenuFactory.createNewFor(this::fsEntry);
        super.setContextMenu(contextMenu);
        super.addEventFilter(CONTEXT_MENU_REQUESTED, event -> {
            if ( isNull(this.fsEntry()) ) {
                event.consume();
            }
        });
    }

    private FSEntry fsEntry() {
        DirectoriesTreeItem item = this.directoriesTreeItem();

        if ( isNull(item) ) {
            return null;
        }

        return item.directory();
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        if ( mouseEvent.isSecondaryButtonDown() ) {
            return;
        }

        if ( super.isEmpty() ) {
            return;
        }

        Object target = mouseEvent.getTarget();
        boolean arrowClicked = false;
        if ( target instanceof StackPane ) {
            StackPane arrowNode = (StackPane) target;
            List<String> styleClasses = arrowNode.getStyleClass();
            if ( styleClasses.contains("tree-disclosure-node") || styleClasses.contains("arrow") ) {
                arrowClicked = true;
            }
        }

        if ( ! arrowClicked ) {
            Directory directory = this.directoriesTreeItem().directory();
            if ( nonNull(directory) ) {
                this.directoriesTree.select(directory);
            }
        }
        mouseEvent.consume();
    }

    private DirectoriesTreeItem directoriesTreeItem() {
        TreeItem<String> item = super.getTreeItem();

        if ( nonNull(item) && item instanceof DirectoriesTreeItem) {
            return (DirectoriesTreeItem) item;
        }
        else {
            return null;
        }
    }

    @Override
    protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);

        if ( empty || name == null ) {
            super.setText(null);
            super.setGraphic(null);
        } else {
            TreeItem<String> item = super.getTreeItem();

            if ( nonNull(item) ) {
                if ( item instanceof DirectoriesTreeItem) {
                    DirectoriesTreeItem directoryTreeItem = (DirectoriesTreeItem) item;
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
        System.out.println("DRAG!");
        if ( super.isEmpty() ) {
            return;
        }

        TreeItem<String> item = super.getTreeItem();

        if ( ! (item instanceof DirectoriesTreeItem) ) {
            return;
        }

        DirectoriesTreeItem directoryItem = (DirectoriesTreeItem) item;

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
            DirectoriesTreeItem droppedItem = droppedCell.directoriesTreeItem();
            DirectoriesTreeItem acceptingItem = this.directoriesTreeItem();

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
            List<FSEntry> fsEntries = this.directoriesTree.dragAndDropFiles().get();

            DirectoriesTreeItem acceptingItem = this.directoriesTreeItem();
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
                DirectoriesTreeItem droppedItem = droppedCell.directoriesTreeItem();
                DirectoriesTreeItem acceptingItem = this.directoriesTreeItem();

                if ( nonNull(droppedItem) ) {
                    if ( nonNull(acceptingItem) ) {
                        Directory droppedDirectory = droppedItem.directory();
                        Directory acceptingDirectory = acceptingItem.directory();
                        acceptingDirectory.host(droppedDirectory);
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
            DirectoriesTreeItem acceptingItem = this.directoriesTreeItem();
            Directory acceptingDirectory = acceptingItem.directory();

            if ( fsEntries.size() == 1 ) {
                FSEntry fsEntryToMove = fsEntries.get(0);
                acceptingDirectory.host(fsEntryToMove);
            }
            else if ( fsEntries.size() > 1 ) {
                acceptingDirectory.hostAll(fsEntries, ProgressTracker.DEFAULT);
            }


            success = true;
        }
        else {
            success = false;
        }

        dragEvent.setDropCompleted(success);
        dragEvent.consume();
    }
}
