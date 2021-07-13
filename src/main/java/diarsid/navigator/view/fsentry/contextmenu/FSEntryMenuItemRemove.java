package diarsid.navigator.view.fsentry.contextmenu;

import javafx.scene.Node;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.filesystem.FileSystem;
import diarsid.support.objects.references.PossibleProperty;

import static java.lang.String.format;

import static diarsid.navigator.filesystem.Directory.Edit.DELETED;
import static diarsid.navigator.filesystem.Directory.Edit.RENAMED;

public class FSEntryMenuItemRemove extends FSEntryMenuItem {

    private final FileSystem fileSystem;

    FSEntryMenuItemRemove(PossibleProperty<FSEntry> fsEntryReference, FileSystem fileSystem) {
        super(fsEntryReference);
        this.fileSystem = fileSystem;
    }

    @Override
    void onAction(FSEntry fsEntry) {
        this.fileSystem.remove(fsEntry);
    }

    @Override
    boolean applicableTo(FSEntry fsEntry) {
        if ( fsEntry.isFile() ) {
            return true;
        }

        Directory directory = fsEntry.asDirectory();

        return directory.canBe(RENAMED) || directory.canBe(DELETED);
    }

    @Override
    String toText(FSEntry fsEntry) {
        return format("remove '%s' %s", fsEntry.name(), fsEntry.isDirectory() ? "directory" : "file");
    }

    @Override
    Node toGraphic(FSEntry fsEntry) {
        return null;
    }
}
