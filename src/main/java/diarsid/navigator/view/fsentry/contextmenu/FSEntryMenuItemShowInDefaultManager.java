package diarsid.navigator.view.fsentry.contextmenu;

import javafx.scene.Node;

import diarsid.filesystem.api.FSEntry;
import diarsid.support.objects.references.PossibleProperty;

import static java.lang.String.format;

class FSEntryMenuItemShowInDefaultManager extends FSEntryMenuItem {

    public FSEntryMenuItemShowInDefaultManager(PossibleProperty<FSEntry> fsEntryReference) {
        super(fsEntryReference);
    }

    @Override
    void onAction(FSEntry entry) {
        entry.showInDefaultFileManager();
    }

    @Override
    boolean applicableTo(FSEntry fsEntry) {
        return true;
    }

    @Override
    String toText(FSEntry fsEntry) {
        return format("show '%s' %s in default manager", fsEntry.name(), fsEntry.isDirectory() ? "directory" : "file");
    }

    @Override
    Node toGraphic(FSEntry fsEntry) {
        return null;
    }
}
