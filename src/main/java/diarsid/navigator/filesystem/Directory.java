package diarsid.navigator.filesystem;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import diarsid.support.objects.groups.Running;

public interface Directory extends FSEntry {

    enum Edit {
        MOVED,
        DELETED,
        RENAMED,
        FILLED
    }

    Optional<Directory> parent();

    default boolean isParentOf(FSEntry fsEntry) {
        return this.isIndirectParentOf(fsEntry) || this.isDirectParentOf(fsEntry);
    }

    boolean isIndirectParentOf(FSEntry fsEntry);

    boolean isIndirectParentOf(Path path);

    boolean isDirectParentOf(FSEntry fsEntry);

    boolean isDirectParentOf(Path path);

    boolean isRoot();

    default boolean isNotRoot() {
        return ! this.isRoot();
    }

    void checkChildrenPresence(Consumer<Boolean> consumer);

    void checkDirectoriesPresence(Consumer<Boolean> consumer);

    void checkFilesPresence(Consumer<Boolean> consumer);

    void feedChildren(Consumer<List<FSEntry>> itemsConsumer);

    void feedDirectories(Consumer<List<Directory>> directoriesConsumer);

    void feedFiles(Consumer<List<File>> filesConsumer);

    void host(FSEntry newEntry, Consumer<Boolean> callback);

    void hostAll(List<FSEntry> newEntries, Consumer<Boolean> callback, ProgressTracker<FSEntry> progressTracker);

    boolean host(FSEntry newEntry);

    boolean hostAll(List<FSEntry> newEntries, ProgressTracker<FSEntry> progressTracker);

    default boolean canHost(FSEntry newEntry) {
        if ( this instanceof LocalMachineDirectory) {
            return false;
        }

        if ( newEntry.isFile() ) {
            return true;
        }

        boolean can = true;
        Directory newDirectory = newEntry.asDirectory();

        if ( newDirectory.equals(this) ) {
            can = false;
        }

        if ( can && newDirectory.isIndirectParentOf(this) ) {
            can = false;
        }

        return can;
    }

    default boolean canNotHost(FSEntry newEntry) {
        return ! this.canHost(newEntry);
    }

    boolean canBe(Directory.Edit edit);

    default boolean canNotBe(Directory.Edit edit) {
        return ! this.canBe(edit);
    }

    void watch();

}
