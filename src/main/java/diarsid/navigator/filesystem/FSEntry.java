package diarsid.navigator.filesystem;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public interface FSEntry extends PathHolder, Comparable<FSEntry> {

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

    String path();

    Optional<Directory> parent();

    List<Directory> parents();

    int depth();

    boolean isHidden();

    boolean moveTo(Directory newPlace);

    boolean remove();

    boolean canBeIgnored();

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
