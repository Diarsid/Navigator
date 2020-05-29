package diarsid.navigator.view.tree;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.view.icons.Icon;
import diarsid.navigator.view.icons.Icons;

import static java.util.Objects.nonNull;
import static javafx.scene.input.TransferMode.MOVE;

public class DirectoriesTreeCell extends TreeCell<String> {

    private final Icons icons;
    private final ImageView iconView;
    private final DirectoriesTree directoriesTree;

    public DirectoriesTreeCell(Icons icons, DirectoriesTree directoriesTree) {
        super();

        this.icons = icons;
        this.directoriesTree = directoriesTree;

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
        this.directoriesTree.dragAndDropContext().setTabDraggedContextTo(this);
//        super.pseudoClassStateChanged(MOVED, true);
        event.consume();
    };

    private void onDragOver(DragEvent dragEvent) {
        if ( this.directoriesTree.dragAndDropContext().isDragOverAcceptable(dragEvent) ) {
            dragEvent.acceptTransferModes(MOVE);
            dragEvent.consume();
//            super.pseudoClassStateChanged(REPLACE_CANDIDATE, true);
        }
    };

    private void onDragExited(DragEvent dragEvent) {
        if ( this.directoriesTree.dragAndDropContext().isDragOverAcceptable(dragEvent) ) {
//            super.pseudoClassStateChanged(REPLACE_CANDIDATE, false);
        }
    };

    private void onDragDropped(DragEvent dragEvent) {
        boolean success;

        boolean isDropAcceptable = this.directoriesTree.dragAndDropContext().isDropAcceptable(dragEvent);
        if ( isDropAcceptable ) {
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
        else {
            success = false;
        }

        dragEvent.setDropCompleted(success);
        dragEvent.consume();
    }
}
