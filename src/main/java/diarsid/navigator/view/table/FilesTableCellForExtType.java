package diarsid.navigator.view.table;

import static javafx.geometry.Pos.CENTER_LEFT;

public class FilesTableCellForExtType extends FilesTableCell<String> {

    public FilesTableCellForExtType() {
        this.setAlignment(CENTER_LEFT);
    }

    @Override
    protected void updateItem(String extType, boolean empty) {
        super.updateItem(extType, empty);

        if (empty || extType == null ) {
            super.setText(null);
        } else {
            FilesTableItem item = super.getTableRow().getItem();
            super.setText(extType);
        }
    }
}
