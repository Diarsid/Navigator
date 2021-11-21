package diarsid.navigator.view.fsentry.contextmenu;

import java.util.function.Consumer;
import java.util.function.Supplier;

import diarsid.filesystem.api.FSEntry;
import diarsid.filesystem.api.FileSystem;

public class FSEntryContextMenuFactory {

    private final FileSystem fileSystem;
    private final Consumer<FSEntry> onIgnore;

    public FSEntryContextMenuFactory(FileSystem fileSystem, Consumer<FSEntry> onIgnore) {
        this.fileSystem = fileSystem;
        this.onIgnore = onIgnore;
    }

    public FSEntryContextMenu createNewFor(Supplier<? extends FSEntry> fsEntrySource) {
        return new FSEntryContextMenu(fsEntrySource::get, this.fileSystem, this.onIgnore);
    }
}
