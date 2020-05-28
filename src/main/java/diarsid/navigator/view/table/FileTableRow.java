package diarsid.navigator.view.table;

import javafx.geometry.Insets;
import javafx.scene.control.TableRow;

class FileTableRow extends TableRow<FileTableItem> {

    FileTableRow() {
        super();
        this.setPadding(new Insets(0, 0, 0, 0));
    }

    @Override
    protected void updateItem(FileTableItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {

        } else {
            item.row().resetTo(this);
        }
    }
}
