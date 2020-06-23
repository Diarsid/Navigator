package diarsid.navigator.view.fsentry.contextmenu;

import java.util.function.BiConsumer;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

import diarsid.navigator.filesystem.FSEntry;

import static java.util.Objects.isNull;

public abstract class FSEntryMenuItem extends MenuItem implements BiConsumer<FSEntry, FSEntry> {

    public FSEntryMenuItem() {
        super();
    }

    @Override
    public final void accept(FSEntry oldFsEntry, FSEntry newFsEntry) {
        super.setText(this.toText(newFsEntry));
        super.setGraphic(this.toGraphic(newFsEntry));
    }

    abstract String toText(FSEntry fsEntry);

    abstract Node toGraphic(FSEntry fsEntry);
}
