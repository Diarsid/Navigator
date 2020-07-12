package diarsid.navigator.filesystem;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import diarsid.navigator.filesystem.ignoring.Ignores;

import static java.util.Objects.isNull;

import static diarsid.navigator.Navigator.NAMED_THREAD_SOURCE;

public interface FileSystem {

    FileSystem INSTANCE = new LocalFileSystem(Ignores.INSTANCE, NAMED_THREAD_SOURCE, FileSystems.getDefault());

    static String getNameFrom(Path path) {
        String name;
        Path fileName = path.getFileName();
        if (isNull(fileName)) {
            name = path.toString();
        }
        else {
            name = fileName.toString();
        }
        return name;
    }

    Directory machineDirectory();

    Optional<FSEntry> toFSEntry(Path path);

    default Optional<Directory> toDirectory(String path) {
        return this.toDirectory(Paths.get(path));
    }

    Optional<Directory> toDirectory(Path path);

    Optional<File> toFile(Path path);

    default Optional<File> toFile(String path) {
        return this.toFile(Paths.get(path));
    }

    boolean isDirectory(Path path);

    boolean isFile(Path path);

    boolean exists(FSEntry fsEntry);

    default boolean isAbsent(FSEntry fsEntry) {
        return ! this.exists(fsEntry);
    }

    boolean copy(FSEntry whatToCopy, Directory parentDirectoryWhereToMove);

    boolean move(FSEntry whatToMove, Directory parentDirectoryWhereToMove);

    boolean rename(FSEntry whatToRename, String newName);

    boolean remove(FSEntry entry);

    boolean copyAll(
            List<FSEntry> whatToCopy,
            Directory parentDirectoryWhereToMove,
            ProgressTracker<FSEntry> progressTracker);

    boolean moveAll(
            List<FSEntry> whatToMove,
            Directory parentDirectoryWhereToMove,
            ProgressTracker<FSEntry> progressTracker);

    boolean removeAll(
            List<FSEntry> entries,
            ProgressTracker<FSEntry> progressTracker);

    boolean open(File file);

    void showInDefaultFileManager(FSEntry fsEntry);

    Stream<FSEntry> list(Directory directory); /* do not forget to close the stream! */

    Optional<Directory> parentOf(FSEntry fsEntry);

    Optional<Directory> existedParentOf(Path path);

    List<Directory> parentsOf(FSEntry fsEntry);

    List<Directory> parentsOf(Path path);

    long sizeOf(FSEntry fsEntry);

    Extensions extensions();

    boolean isRoot(Directory directory);

    boolean isMachine(Directory directory);

    default boolean isNotMachine(Directory directory) {
        return ! this.isMachine(directory);
    }

    FileSystemType type();

    Changes changes();

    void watch(Directory directory);

}
