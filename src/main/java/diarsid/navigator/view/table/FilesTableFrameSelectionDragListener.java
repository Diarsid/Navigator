package diarsid.navigator.view.table;

import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;

import diarsid.support.javafx.ClickOrDragDetector;

import static java.util.Objects.isNull;

public class FilesTableFrameSelectionDragListener implements ClickOrDragDetector.DragListener {

    private final TableView<FileTableItem> tableView;
    private final FilesTableFrameSelection selection;
    private VirtualFlow<FileTableRow> tableViewRows;

    public FilesTableFrameSelectionDragListener(
            TableView<FileTableItem> tableView, FilesTableFrameSelection selection) {
        this.tableView = tableView;
        this.selection = selection;
    }

    @Override
    public void onDragStart(MouseEvent mouseEvent) {
        if ( isNull(this.tableViewRows) ) {
            this.setTableViewRows();
        }
        Bounds bounds = this.tableViewRows.localToScene(this.tableViewRows.getBoundsInLocal());
        this.selection.start(mouseEvent, bounds);
    }

    private List<FileTableItem> items() {
        return this.tableView.getItems();
    }

    private void select(FileTableRow row) {
        this.tableView.getSelectionModel().select(row.getIndex());
    }

    private void unselect(FileTableRow row) {
        this.tableView.getSelectionModel().clearSelection(row.getIndex());
    }

    private void checkForSelection(FileTableItem tableItem) {
        FileTableRow row = tableItem.row().orThrow();
        if (this.selection.isIntersectedWith(row)) {
            this.select(row);
        } else {
            this.unselect(row);
        }
    }

    @Override
    public void onDragging(MouseEvent mouseEvent) {
        this.selection.dragged(mouseEvent);

        this.items().forEach(this::checkForSelection);
    }

    @Override
    public void onDragStopped(MouseEvent mouseEvent) {
        System.out.println("table drag - stopped");

        List<FileTableItem> selected = tableView.getSelectionModel().getSelectedItems();

        this.selection.stop(mouseEvent);
    }

    @SuppressWarnings("unchecked")
    private void setTableViewRows() {
        this.tableViewRows = this.tableView
                .getChildrenUnmodifiable()
                .stream()
                .filter(node -> node instanceof VirtualFlow)
                .map(node -> (VirtualFlow<FileTableRow>) node)
                .findFirst()
                .get();
    }
}
