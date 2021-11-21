package diarsid.navigator.view.fsentry.contextmenu;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

import diarsid.filesystem.api.FSEntry;
import diarsid.support.objects.references.PossibleProperty;

import static java.util.Objects.isNull;

abstract class FSEntryMenuItem extends MenuItem {

    private final PossibleProperty<FSEntry> fsEntry;

    FSEntryMenuItem(PossibleProperty<FSEntry> fsEntryReference) {
        super();
        this.setOnAction(this::doOnActionInternally);
        this.fsEntry = fsEntryReference;
        this.fsEntry.listen(this::onChange);
    }

    private void onChange(FSEntry oldFsEntry, FSEntry newFsEntry) {
        if ( isNull(newFsEntry) ) {
            super.setText(null);
            super.setGraphic(null);
        }
        else {
            super.setText(this.toText(newFsEntry));
            super.setGraphic(this.toGraphic(newFsEntry));

            if ( this.applicableTo(newFsEntry) ) {
                super.setDisable(false);
            }
            else {
                super.setDisable(true);
            }
        }
    }

    private void doOnActionInternally(ActionEvent actionEvent) {
        this.fsEntry.ifPresent(this::onAction);
    }

    abstract void onAction(FSEntry fsEntry);

    abstract boolean applicableTo(FSEntry fsEntry);

    abstract String toText(FSEntry fsEntry);

    abstract Node toGraphic(FSEntry fsEntry);
}
