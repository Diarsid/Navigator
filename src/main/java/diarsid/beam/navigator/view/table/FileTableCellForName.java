package diarsid.beam.navigator.view.table;

import java.util.concurrent.atomic.AtomicLong;
import javafx.scene.control.TableCell;

import static javafx.geometry.Pos.CENTER_LEFT;

public class FileTableCellForName extends TableCell<FileTableItem, String> {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    public FileTableCellForName() {
        this.setAlignment(CENTER_LEFT);
    }

    @Override
    protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);


        if (empty || name == null ) {
            super.setText(null);
        } else {
//            FileTableItem item = super.getTableRow().getItem();
            super.setText(name);
        }
    }
}
