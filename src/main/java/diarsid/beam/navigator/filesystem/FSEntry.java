package diarsid.beam.navigator.filesystem;

import static java.lang.String.format;

public interface FSEntry extends Comparable<FSEntry> {

    String name();

    String fullName();

    boolean isDirectory();

    boolean isFile();

    boolean isHidden();

    default String getName() {
        return this.name();
    }

    default Integer getSize() {
        return 100;
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
