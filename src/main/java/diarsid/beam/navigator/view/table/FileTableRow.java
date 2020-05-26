package diarsid.beam.navigator.view.table;

import java.util.concurrent.atomic.AtomicLong;
import javafx.geometry.Insets;
import javafx.scene.control.TableRow;

import static java.lang.String.format;

class FileTableRow extends TableRow<FileTableItem> {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    FileTableRow() {
        super();
        this.setPadding(new Insets(0, 0, 0, 0));
        System.out.println(format("%s:%s created", this.getClass().getSimpleName(), COUNTER.incrementAndGet()));
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
