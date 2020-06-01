package diarsid.navigator.filesystem;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import diarsid.support.objects.groups.Running;

public interface Directory extends FSEntry {

    enum Edit {
        MOVE,
        DELETE,
        RENAME,
        FILLED
    }

    Optional<Directory> parent();

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

    boolean host(FSEntry newEntry);

    Running listenForChanges(Runnable listener);

    boolean canBe(Directory.Edit edit);

}
