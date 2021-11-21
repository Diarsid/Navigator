package diarsid.navigator.view.fsentry.contextmenu;

import java.util.function.Consumer;
import javafx.scene.Node;

import diarsid.filesystem.api.FSEntry;
import diarsid.support.objects.references.PossibleProperty;

import static java.lang.String.format;

class FSEntryMenuItemIgnore extends FSEntryMenuItem {

    private final Consumer<FSEntry> onIgnore;

    FSEntryMenuItemIgnore(PossibleProperty<FSEntry> fsEntryReference, Consumer<FSEntry> onIgnore) {
        super(fsEntryReference);
        this.onIgnore = onIgnore;
    }

    @Override
    void onAction(FSEntry fsEntry) {
        this.onIgnore.accept(fsEntry);
    }

    @Override
    boolean applicableTo(FSEntry fsEntry) {
        return fsEntry.canBeIgnored();
    }

    @Override
    String toText(FSEntry fsEntry) {
        return format("ignore '%s' %s", fsEntry.name(), fsEntry.isDirectory() ? "directory" : "file");
    }

    @Override
    Node toGraphic(FSEntry fsEntry) {
        return null;
    }
}
