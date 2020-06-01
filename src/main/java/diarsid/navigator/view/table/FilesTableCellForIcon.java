package diarsid.navigator.view.table;

import javafx.scene.image.ImageView;

import static javafx.geometry.Pos.CENTER;

public class FilesTableCellForIcon extends FilesTableCell<ImageView> {

    public FilesTableCellForIcon() {
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
