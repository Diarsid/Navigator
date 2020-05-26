package diarsid.beam.navigator.view.table;

import java.util.List;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

import diarsid.support.javafx.ClickOrDragDetector;

public class FilesTableFrameSelectionDragListener implements ClickOrDragDetector.DragListener {

    private final TableView<FileTableItem> tableView;
    private final FilesTableFrameSelection selection;

    public FilesTableFrameSelectionDragListener(
            TableView<FileTableItem> tableView, FilesTableFrameSelection selection) {
        this.tableView = tableView;
        this.selection = selection;
    }

    @Override
    public void onDragStart(MouseEvent mouseEvent) {
        this.selection.start(mouseEvent);
    }

    @Override
    public void onDragging(MouseEvent mouseEvent) {
        this.selection.dragged(mouseEvent);

        tableView.getItems().forEach(tableItem -> {
            FileTableRow row = tableItem.row().orThrow();
            if ( this.selection.isIntersectedWith(row) ) {
                tableView.getSelectionModel().select(row.getIndex());
            }
            else {
                tableView.getSelectionModel().clearSelection(row.getIndex());
            }
        });
    }

    @Override
    public void onDragStopped(MouseEvent mouseEvent) {
        System.out.println("table drag - stopped");

        List<FileTableItem> selected = tableView.getSelectionModel().getSelectedItems();

        this.selection.stop(mouseEvent);
    }
}
