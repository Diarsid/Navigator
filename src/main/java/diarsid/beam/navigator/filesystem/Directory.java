package diarsid.beam.navigator.filesystem;

import java.util.List;
import java.util.function.Consumer;

public interface Directory extends FSEntry {

    void checkChildrenPresence(Consumer<Boolean> consumer);

    void checkDirectoriesPresence(Consumer<Boolean> consumer);

    void checkFilesPresence(Consumer<Boolean> consumer);

    void feedChildren(Consumer<List<FSEntry>> itemsConsumer);

    void feedDirectories(Consumer<List<Directory>> directoriesConsumer);

    void feedFiles(Consumer<List<File>> filesConsumer);

}
