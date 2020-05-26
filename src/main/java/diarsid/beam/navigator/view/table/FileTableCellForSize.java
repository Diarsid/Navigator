package diarsid.beam.navigator.view.table;

import javafx.scene.control.TableCell;

import static javafx.geometry.Pos.CENTER_RIGHT;

public class FileTableCellForSize extends TableCell<FileTableItem, String> {

    public FileTableCellForSize() {
        this.setAlignment(CENTER_RIGHT);
    }

    @Override
    protected void updateItem(String sizeFormat, boolean empty) {
        super.updateItem(sizeFormat, empty);

        if (empty || sizeFormat == null ) {
            super.setText(null);
        } else {
            FileTableItem item = super.getTableRow().getItem();
            super.setText(sizeFormat);
        }
    }
}
