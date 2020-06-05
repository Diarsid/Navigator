package diarsid.navigator.filesystem;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import diarsid.navigator.filesystem.ignoring.Ignores;

public interface FileSystem {

    FileSystem INSTANCE = new LocalFileSystem(Ignores.INSTANCE, FileSystems.getDefault());

    Directory machineDirectory();

    FSEntry toFSEntry(Path path);

    default Directory toDirectory(String path) {
        return this.toDirectory(Paths.get(path));
    }

    Directory toDirectory(Path path);

    File toFile(Path path);

    default File toFile(String path) {
        return this.toFile(Paths.get(path));
    }

    boolean isDirectory(Path path);

    boolean isFile(Path path);

    boolean copy(FSEntry whatToCopy, Directory parentDirectoryWhereToMove);

    boolean move(FSEntry whatToMove, Directory parentDirectoryWhereToMove);

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

    Stream<FSEntry> list(Directory directory); /* do not forget to close the stream! */

    List<Directory> parentsOf(FSEntry fsEntry);

    long sizeOf(FSEntry fsEntry);

    Extensions extensions();

    boolean isRoot(Directory directory);

    boolean isMachine(Directory directory);

    FileSystemType type();

}
