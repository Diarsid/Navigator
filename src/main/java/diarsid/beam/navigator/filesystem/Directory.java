package diarsid.beam.navigator.filesystem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface Directory extends FSEntry {

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

    UUID listenForChanges(Consumer<Directory> listener);

    Consumer<Directory> removeListener(UUID uuid);

}
