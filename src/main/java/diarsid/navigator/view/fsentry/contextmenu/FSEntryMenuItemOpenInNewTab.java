package diarsid.navigator.view.fsentry.contextmenu;

import java.util.function.Consumer;
import javafx.scene.Node;

import diarsid.filesystem.api.Directory;
import diarsid.filesystem.api.FSEntry;
import diarsid.support.objects.references.PossibleProperty;

import static java.lang.String.format;

public class FSEntryMenuItemOpenInNewTab extends FSEntryMenuItem {

    private final Consumer<Directory> onOpenInNewTab;

    FSEntryMenuItemOpenInNewTab(PossibleProperty<FSEntry> fsEntryReference, Consumer<Directory> onOpenInNewTab) {
        super(fsEntryReference);
        this.onOpenInNewTab = onOpenInNewTab;
    }

    @Override
    void onAction(FSEntry fsEntry) {
        if ( fsEntry.isFile() ) {
            return;
        }

        this.onOpenInNewTab.accept(fsEntry.asDirectory());
    }

    @Override
    boolean applicableTo(FSEntry fsEntry) {
        return fsEntry.isNotHidden() && fsEntry.isDirectory();
    }

    @Override
    String toText(FSEntry fsEntry) {
        return format("open %s '%s' in new tab ", fsEntry.isDirectory() ? "directory" : "file", fsEntry.name());
    }

    @Override
    Node toGraphic(FSEntry fsEntry) {
        return null;
    }
}
