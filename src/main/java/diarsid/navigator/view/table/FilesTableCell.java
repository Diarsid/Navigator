package diarsid.navigator.view.table;

import javafx.scene.control.TableCell;

import static java.util.Objects.isNull;

public abstract class FilesTableCell<T> extends TableCell<FileTableItem, T> {

    @Override
    public String toString() {
        String format = this.getClass().getSimpleName() + "{_}";

        FileTableRow row = (FileTableRow) super.getTableRow();

        if ( row.isEmpty() ) {
            return format.replace("_", "empty");
        }

        FileTableItem item = row.getItem();

        if ( isNull(item) ) {
            return format.replace("_", "null");
        }

        return format.replace("_", item.fsEntry().name());
    }
}
