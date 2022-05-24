package diarsid.navigator.view.table;


import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

import diarsid.filesystem.api.FSEntry;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static javafx.geometry.Pos.CENTER_LEFT;

public class FilesTableCellForName extends FilesTableCell<String> {

    private final UUID uuid;
    private final TextField editField;
    private final BiConsumer<FSEntry, String> onRename;

    public FilesTableCellForName(BiConsumer<FSEntry, String> onRename) {
        this.setAlignment(CENTER_LEFT);
        this.uuid = randomUUID();
        this.editField = new TextField();
        this.editField.getStyleClass().add("edit");
        this.onRename = onRename;

        this.editField.setOnAction(this::onActionCommitEdit);

        super.setEditable(true);
    }

    private void onActionCommitEdit(ActionEvent event) {
        this.commitEdit(this.editField.getText());
    }

    @Override
    protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);

        if (empty || name == null ) {
            super.setText(null);
        }
        else {
//            FileTableItem item = super.getTableRow().getItem();
            super.setText(name);
            this.editField.setText(name);
        }
    }

    @Override
    public void startEdit() {
        FSEntry entry = super.getTableRow().getItem().fsEntry();
        super.setText(null);
        this.editField.setText(entry.name());
        super.setGraphic(this.editField);
        this.editField.requestFocus();
        this.editField.selectAll();
        super.startEdit();
    }

    @Override
    public void commitEdit(String newValue) {
        FSEntry entry = super.getTableRow().getItem().fsEntry();
        super.commitEdit(newValue);
        super.setText(entry.name());
        super.setGraphic(null);
        this.editField.setText(null);
        this.onRename.accept(entry, newValue);
    }

    @Override
    public void cancelEdit() {
        FilesTableItem item = super.getTableRow().getItem();
        if ( isNull(item) ) {
            super.setText(null);
        }
        else {
            String name = item.fsEntry().name();
            super.setText(name);
        }

        this.editField.setText(null);
        super.setGraphic(null);
        super.cancelEdit();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilesTableCellForName)) return false;
        FilesTableCellForName that = (FilesTableCellForName) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
