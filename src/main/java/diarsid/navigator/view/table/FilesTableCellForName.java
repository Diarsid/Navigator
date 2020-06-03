package diarsid.navigator.view.table;

import java.util.concurrent.atomic.AtomicLong;

import static javafx.geometry.Pos.CENTER_LEFT;

public class FilesTableCellForName extends FilesTableCell<String> {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    public FilesTableCellForName() {
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
