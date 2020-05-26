package diarsid.beam.navigator.filesystem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;

public interface FSEntry extends PathHolder, Comparable<FSEntry> {

    String name();

    boolean isDirectory();

    boolean isFile();

    String path();

    Optional<Directory> parent();

    List<Directory> parents();

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
