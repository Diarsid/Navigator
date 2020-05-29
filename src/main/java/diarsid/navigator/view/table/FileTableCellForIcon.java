package diarsid.navigator.view.table;

import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;

import static javafx.geometry.Pos.CENTER;

public class FileTableCellForIcon extends TableCell<FileTableItem, ImageView> {

    public FileTableCellForIcon() {
        this.setAlignment(CENTER);
    }

    @Override
    protected void updateItem(ImageView icon, boolean empty) {
        super.updateItem(icon, empty);

        if (empty || icon == null ) {
            super.setGraphic(null);
        } else {
            FileTableItem item = super.getTableRow().getItem();
            super.setGraphic(icon);
        }
    }
}
