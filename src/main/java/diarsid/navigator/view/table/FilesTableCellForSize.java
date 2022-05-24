package diarsid.navigator.view.table;

import static javafx.geometry.Pos.CENTER_RIGHT;

public class FilesTableCellForSize extends FilesTableCell<String> {

    public FilesTableCellForSize() {
        this.setAlignment(CENTER_RIGHT);
    }

    @Override
    protected void updateItem(String sizeFormat, boolean empty) {
        super.updateItem(sizeFormat, empty);

        if (empty || sizeFormat == null ) {
            super.setText(null);
        }
        else {
            FilesTableItem item = super.getTableRow().getItem();
            super.setText(sizeFormat);
        }
    }
}
