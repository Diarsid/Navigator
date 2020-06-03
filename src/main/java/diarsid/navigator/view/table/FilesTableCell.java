package diarsid.navigator.view.table;

import javafx.scene.control.TableCell;

import static java.util.Objects.isNull;

public abstract class FilesTableCell<T> extends TableCell<FilesTableItem, T> {

    @Override
    public String toString() {
        String format = this.getClass().getSimpleName() + "{_}";

        FilesTableRow row = (FilesTableRow) super.getTableRow();

        if ( row.isEmpty() ) {
            return format.replace("_", "empty");
        }

        FilesTableItem item = row.getItem();

        if ( isNull(item) ) {
            return format.replace("_", "null");
        }

        return format.replace("_", item.fsEntry().name());
    }
}
