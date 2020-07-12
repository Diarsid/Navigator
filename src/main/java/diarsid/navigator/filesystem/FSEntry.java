package diarsid.navigator.filesystem;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public interface FSEntry extends Comparable<FSEntry> {

    Comparator<FSEntry> compareByDepth = (fsEntry1, fsEntry2) -> {
        if ( fsEntry1.depth() > fsEntry2.depth() ) {
            return -1;
        }
        else if ( fsEntry1.depth() < fsEntry2.depth() ) {
            return 1;
        }
        else {
            return 0;
        }
    };

    String name();

    boolean isDirectory();

    boolean isFile();

    Path path();

    void showInDefaultFileManager();

    default boolean has(Path path) {
        return this.path().equals(path);
    }

    default boolean hasNot(Path path) {
        return ! this.path().equals(path);
    }

    Optional<Directory> parent();

    Optional<Directory> existedParent();

    List<Directory> parents();

    default boolean isDescendantOf(Path path) {
        return this.path().startsWith(path) && this.hasNot(path);
    }

    default boolean isDescendantOf(Directory directory) {
        return directory.isParentOf(this);
    }

    int depth();

    boolean isHidden();

    boolean remove();

    boolean moveTo(Directory newPlace);

    boolean canBeIgnored();

    boolean exists();

    boolean isAbsent();

    FileSystem fileSystem();

    default String getName() {
        return this.name();
    }

    default boolean isNotHidden() {
        return ! this.isHidden();
    }

    default Directory asDirectory() {
        if (this.isDirectory()) {
            return (Directory) this;
        }

        throw new IllegalStateException(format("%s is not a directory!", this.name()));
    }

    default File asFile() {
        if (this.isFile()) {
            return (File) this;
        }

        throw new IllegalStateException(format("%s is not a file!", this.name()));
    }
}
