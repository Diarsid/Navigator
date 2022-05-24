package diarsid.navigator.view.table;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.robot.Robot;
import javafx.scene.text.Text;

import diarsid.filesystem.api.FSEntry;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.support.javafx.ClickOrDragDetector;
import diarsid.support.javafx.FrameSelection;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.filesystem.api.Directory.Edit.MOVED;

public class FilesTableFrameSelectionDragScrollListener implements ClickOrDragDetector.DragListener {

    static class ScrollSession {

        final TableView<FilesTableItem> tableView;
        TableRow<FilesTableItem> firstRow;
        TableRow<FilesTableItem> lastRow;
        int firstIndex;
        int lastIndex;

        public ScrollSession(TableView<FilesTableItem> tableView) {
            this.tableView = tableView;
        }

        void startWith(TableRow<FilesTableItem> row) {
            firstRow = row;
            firstIndex = firstRow.getIndex();
        }

        void proceedWith(TableRow<FilesTableItem> row) {
            if ( isNull(row) ) {
                return;
            }

            if ( isNull(firstRow) ) {
                return;
            }

            lastRow = row;
            lastIndex = lastRow.getIndex();

            int indexFrom;
            int indexTo;
            int diff;
            if ( firstIndex < lastIndex ) {
                indexFrom = firstIndex;
                indexTo = lastIndex;
            }
            else {
                indexFrom = lastIndex;
                indexTo = firstIndex;
            }
            diff = indexTo - indexFrom;

            this.tableView.getSelectionModel().clearSelection();
            if ( diff == 0 ) {
                this.tableView.getSelectionModel().select(indexFrom);
            }
            else if ( diff == 1 ) {
                this.tableView.getSelectionModel().select(indexFrom);
                this.tableView.getSelectionModel().select(indexFrom + 1);
            }
            else {
                for (int i = indexFrom; i <= indexTo; i++) {
                    this.tableView.getSelectionModel().select(i);
                }
            }
        }

        void stop() {
            firstRow = null;
            firstIndex = -1;
            lastIndex = -1;
        }
    }

    private enum DragMode {
        NONE,
        SELECT,
        MOVE
    }

    private final Supplier<Boolean> isDraggingBehaviorAllowed;
    private final TableView<FilesTableItem> tableView;
    private final FrameSelection selection;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;
    private final ScrollSession selectSession;
    private VirtualFlow<FilesTableRow> tableViewRows;
    private DragMode dragMode;


    public FilesTableFrameSelectionDragScrollListener(
            Supplier<Boolean> isDraggingBehaviorAllowed,
            TableView<FilesTableItem> tableView,
            FrameSelection selection,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        this.isDraggingBehaviorAllowed = isDraggingBehaviorAllowed;
        this.tableView = tableView;
        this.selection = selection;
        this.dragAndDropFiles = dragAndDropFiles;
        this.selectSession = new ScrollSession(this.tableView);
        this.dragMode = DragMode.NONE;
    }

    @Override
    public void onPreClicked(MouseEvent mouseEvent) {
        if ( ! this.isDraggingBehaviorAllowed.get() ) {
            this.dragMode = DragMode.NONE;
            return;
        }

        TableRow<FilesTableItem> row = this.getClickedRowFrom(mouseEvent);

        if ( isNull(row) ) {
            this.dragMode = DragMode.NONE;
            return;
        }

        if ( row.isSelected() ) {
            this.dragMode = DragMode.MOVE;
        }
        else {
            this.dragMode = DragMode.SELECT;
        }
    }

    @Override
    public void onDragStart(MouseEvent mouseEvent) {
        if ( isNull(this.tableViewRows) ) {
            this.setTableViewRows();
        }

        switch ( this.dragMode ) {
            case NONE:
                break;
            case SELECT:
                Bounds bounds = this.tableViewRows.localToScene(this.tableViewRows.getBoundsInLocal());
                this.selection.start(mouseEvent, bounds);
                FilesTableRow selectedRow = this.getClickedRowFrom(mouseEvent);
                if ( nonNull(selectedRow) ) {
                    this.selectSession.startWith(selectedRow);
                }
                break;
            case MOVE:
                AtomicBoolean containsNotMovableEntries = new AtomicBoolean(false);

                List<FSEntry> entries = this.tableView
                        .getSelectionModel()
                        .getSelectedItems()
                        .stream()
                        .map(FilesTableItem::fsEntry)
                        .peek(fsEntry -> {
                            if ( containsNotMovableEntries.get() ) {
                                return;
                            }

                            if ( fsEntry.isDirectory() ) {
                                containsNotMovableEntries.set(fsEntry.asDirectory().canNotBe(MOVED));
                            }
                        })
                        .collect(toList());

                if ( containsNotMovableEntries.get() ) {
                    return;
                }

                FilesTableRow row = this.getClickedRowFrom(mouseEvent);
                if ( nonNull(row) ) {
                    this.dragAndDropFiles.startDragAndDrop(row, entries);
                }

                break;
            default:
                throwBehaviorNotSpecifiedException(this.dragMode);
        }
    }

    @Override
    public void onDragging(MouseEvent mouseEvent) {
        switch ( this.dragMode ) {
            case NONE:
                // do nothing
                break;
            case SELECT:
                this.selection.dragged(mouseEvent);
                var row = getHoveredRowFrom(mouseEvent);
                if ( nonNull(row) ) {
                    this.selectSession.proceedWith(row);
                }
                break;
            case MOVE:
                break;
            default:
                throwBehaviorNotSpecifiedException(this.dragMode);
        }
    }

    private final Robot robot = new Robot();

    public void onScrolled(ScrollEvent scrollEvent, FilesTableRow rowScrolled) {
        if ( this.selectSession.firstRow == null ) {
            return;
        }
        double rowHeight = rowScrolled.getHeight();
        double deltaY = scrollEvent.getDeltaY();

        boolean isScrollNegative = deltaY < 0;

        double deltaYAbs = Math.abs(deltaY);
        int scrolledRows = (int) (deltaYAbs / rowHeight);
        double remain = deltaYAbs % rowHeight;

        if ( remain != 0 ) {
            scrolledRows++;
        }
        if ( scrolledRows < 3 ) {
            scrolledRows++;
        }


        double scrollY;
        if ( isScrollNegative ) {
            scrollY = scrolledRows * rowHeight * -1;
        }
        else {
            scrollY = scrolledRows * rowHeight;
        }

        this.selection.scrolled(
                scrollEvent.getSceneX(),
                scrollEvent.getSceneY(),
                scrollEvent.getDeltaX(),
                scrollY);

        Platform.requestNextPulse();
        var targetRow = getRowFrom(scrollEvent.getTarget());
        var pickedRow = getRowFrom(scrollEvent.getPickResult().getIntersectedNode());
        if ( nonNull(pickedRow) && nonNull(targetRow) ) {


            int rowAdjustedIndex;
            if ( isScrollNegative ) {
                rowAdjustedIndex = pickedRow.getIndex() + scrolledRows;
                int lastRowIndex = this.tableView.getItems().size() - 1;
                if ( rowAdjustedIndex > lastRowIndex ) {
                    rowAdjustedIndex = lastRowIndex;
                }
            }
            else {
                rowAdjustedIndex = pickedRow.getIndex() - scrolledRows;
                if ( rowAdjustedIndex < 0 ) {
                    rowAdjustedIndex = 0;
                }
            }

            System.out.println(format("SCROLL scrolledRows:%s, scrollY:%s, deltaY:%s", scrolledRows, scrollY, deltaY));

            this.tableView
                    .getItems()
                    .get(rowAdjustedIndex)
                    .row()
                    .ifPresent(this.selectSession::proceedWith);

//            Bounds rowBounds = pickedRow.localToScreen(pickedRow.getBoundsInLocal());
//            System.out.println(format("SCROLL OVER %s rowY(Screen):%s-%s, mouseY:%s, mouseY(Screen):%s",
//                    pickedRow.getItem().getName(),
//                    rowBounds.getMinY(), rowBounds.getMaxY(),
//                    scrollEvent.getSceneY(),
//                    robot.getMouseY()));


        }

//        runAsync(() -> {
//            Platform.runLater(() -> {
//                synchronized ( this.selection ) {
////                    var pickedRow = getRowFrom(scrollEvent.getPickResult().getIntersectedNode());
////                    if ( nonNull(pickedRow) ) {
////                        System.out.println("SCROLL OVER " + pickedRow.getItem().getName());
////                        this.selectSession.proceedWith(pickedRow);
////                    }
//
////                    this.tableView.getItems().forEach(item -> {
////                        if ( item.row().isPresent() ) {
////                            FilesTableRow row = item.row().orThrow();
////                            if ( this.selection.isIntersectedWith(row) ) {
//////                                System.out.println(uuid.toString() + " ");
////                                this.selectSession.proceedWith(row);
////
//////                                index = row.getIndex();
//////
//////                                this.tableView.getSelectionModel().select(index);
////
//////                                if ( this.scrollSelectionMinIndex.get() < 0 ) {
//////                                    this.scrollSelectionMinIndex.set(index);
//////                                }
//////
//////                                if ( this.scrollSelectionMinIndex.get() > index ) {
//////                                    this.scrollSelectionMinIndex.set(index);
//////                                }
//////
//////                                if ( this.scrollSelectionMaxIndex.get() < 0 ) {
//////                                    this.scrollSelectionMaxIndex.set(index);
//////                                }
//////
//////                                if ( this.scrollSelectionMaxIndex.get() < index ) {
//////                                    this.scrollSelectionMaxIndex.set(index);
//////                                }
//////
//////                                System.out.println(format("SELECT ALL FROM %s TO %s",
//////                                        this.scrollSelectionMinIndex.get(),
//////                                        this.scrollSelectionMaxIndex.get()));
//////                                for ( int selectIndex = this.scrollSelectionMinIndex.get(); selectIndex <= this.scrollSelectionMaxIndex.get(); selectIndex++) {
//////                                    this.tableView.getSelectionModel().select(selectIndex);
//////                                }
////                            }
////                        }
////                    });
//                }
//
//            });
//        });
    }

    @Override
    public void onDragStopped(MouseEvent mouseEvent) {
        switch ( this.dragMode ) {
            case NONE:
                // do nothing
                break;
            case SELECT:
                System.out.println("SELECTION STOPPED");
                this.selectSession.stop();
                this.selection.stop(mouseEvent);
                break;
            case MOVE:
                break;
            default:
                throwBehaviorNotSpecifiedException(this.dragMode);
        }

        this.dragMode = DragMode.NONE;
    }

    private FilesTableRow getClickedRowFrom(MouseEvent mouseEvent) {
        Object target = mouseEvent.getTarget();

        return getRowFrom(target);
    }


    private FilesTableRow getHoveredRowFrom(MouseEvent mouseEvent) {
        PickResult pickResult = mouseEvent.getPickResult();
        Node pickedNode = pickResult.getIntersectedNode();

        return getRowFrom(pickedNode);
    }

    @SuppressWarnings("unchecked")
    private static FilesTableRow getRowFrom(Object target) {
        TableRow<FilesTableItem> row;

        if (target instanceof FilesTableCell) {
            FilesTableCell<Object> draggedCell = (FilesTableCell<Object>) target;
            row = draggedCell.getTableRow();
        } else if (target instanceof TableRow) {
            row = (TableRow<FilesTableItem>) target;
        } else if (target instanceof Text) {
            Text text = (Text) target;
            Node possibleCell = text.getParent();
            if (possibleCell instanceof FilesTableCell) {
                FilesTableCell<Object> draggedCell = (FilesTableCell<Object>) possibleCell;
                row = draggedCell.getTableRow();
            } else {
                row = null;
            }
        } else {
            row = null;
        }

        if (nonNull(row)) {
            return (FilesTableRow) row;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void setTableViewRows() {
        this.tableViewRows = this.tableView
                .getChildrenUnmodifiable()
                .stream()
                .filter(node -> node instanceof VirtualFlow)
                .map(node -> (VirtualFlow<FilesTableRow>) node)
                .findFirst()
                .get();
    }

    private List<FilesTableItem> items() {
        return this.tableView.getItems();
    }

    private void select(FilesTableRow row) {
        this.tableView.getSelectionModel().select(row.getIndex());
    }

    private void unselect(FilesTableRow row) {
//        System.out.println("UNSELECT " + row.getItem().getName());
        this.tableView.getSelectionModel().clearSelection(row.getIndex());
    }

    private void checkForSelection(FilesTableItem tableItem) {
        if ( tableItem.row().isPresent() ) {
            FilesTableRow row = tableItem.row().orThrow();
            if (this.selection.isIntersectedWith(row)) {
                this.select(row);
            } else {
                this.unselect(row);
            }
        }
    }

    private void throwBehaviorNotSpecifiedException(DragMode dragMode) {
        throw new UnsupportedOperationException("Unknown drag mode: " + dragMode);
    }
}
