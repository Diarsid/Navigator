package diarsid.navigator.view.table;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.geometry.Bounds;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.support.javafx.ClickOrDragDetector;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.navigator.filesystem.Directory.Edit.MOVED;

public class FilesTableFrameSelectionDragListener implements ClickOrDragDetector.DragListener {

    private enum DragMode {
        NONE,
        SELECT,
        MOVE
    }

    private final TableView<FilesTableItem> tableView;
    private final FilesTableFrameSelection selection;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;
    private VirtualFlow<FilesTableRow> tableViewRows;
    private DragMode dragMode;

    public FilesTableFrameSelectionDragListener(
            TableView<FilesTableItem> tableView,
            FilesTableFrameSelection selection,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        this.tableView = tableView;
        this.selection = selection;
        this.dragAndDropFiles = dragAndDropFiles;
        this.dragMode = DragMode.NONE;
    }

    @Override
    public void onPreClicked(MouseEvent mouseEvent) {
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

        System.out.println("DRAG MODE : " + this.dragMode);
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
                this.items().forEach(this::checkForSelection);
                break;
            case MOVE:
                break;
            default:
                throwBehaviorNotSpecifiedException(this.dragMode);
        }
    }

    @Override
    public void onDragStopped(MouseEvent mouseEvent) {
        switch ( this.dragMode ) {
            case NONE:
                // do nothing
                break;
            case SELECT:
                this.selection.stop(mouseEvent);
                break;
            case MOVE:
                List<FilesTableItem> selected = this.tableView.getSelectionModel().getSelectedItems();
                System.out.println("STOPPED");
                break;
            default:
                throwBehaviorNotSpecifiedException(this.dragMode);
        }

        this.dragMode = DragMode.NONE;
    }

    @SuppressWarnings("unchecked")
    private FilesTableRow getClickedRowFrom(MouseEvent mouseEvent) {
        Object target = mouseEvent.getTarget();

        TableRow<FilesTableItem> row;

        if ( target instanceof FilesTableCell ) {
            FilesTableCell<Object> draggedCell = (FilesTableCell<Object>) target;
            row = draggedCell.getTableRow();
        }
        else if ( target instanceof TableRow ) {
            row = (TableRow<FilesTableItem>) target;
        }
        else if ( target instanceof Text) {
            Text text = (Text) target;
            FilesTableCell<Object> draggedCell = (FilesTableCell<Object>) text.getParent();
            row = draggedCell.getTableRow();
        }
        else {
            row = null;
        }

        if ( nonNull(row) ) {
            return (FilesTableRow) row;
        }
        else {
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
        this.tableView.getSelectionModel().clearSelection(row.getIndex());
    }

    private void checkForSelection(FilesTableItem tableItem) {
        FilesTableRow row = tableItem.row().orThrow();
        if (this.selection.isIntersectedWith(row)) {
            this.select(row);
        } else {
            this.unselect(row);
        }
    }

    private void throwBehaviorNotSpecifiedException(DragMode dragMode) {
        throw new UnsupportedOperationException("Unknown drag mode: " + dragMode);
    }
}
