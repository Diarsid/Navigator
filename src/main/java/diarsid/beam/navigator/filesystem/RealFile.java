package diarsid.beam.navigator.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;

class RealFile implements File {

    private final Path path;
    private final String name;
    private final String fullName;
    private final FS fs;

    RealFile(Path path, FS fs) {
        this.path = path;
        this.name = path.getFileName().toString();
        this.fullName = path.toAbsolutePath().toString();
        this.fs = fs;
        fs.isFile(this.path);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String path() {
        return this.fullName;
    }

    @Override
    public Optional<Directory> parent() {
        Path parent = this.path.getParent();
        if ( nonNull(parent) ) {
            return Optional.of(fs.toDirectory(parent));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public List<Directory> parents() {
        return this.fs.parentsOf(this);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isHidden() {
        try {
            return Files.isHidden(this.path);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean moveTo(Directory newPlace) {
        return this.fs.move(this, newPlace);
    }

    @Override
    public boolean remove() {
        return this.fs.remove(this);
    }

    @Override
    public boolean canBeIgnored() {
        return true;
    }

    @Override
    public long size() {
        return this.fs.sizeOf(this);
    }

    @Override
    public Path nioPath() {
        return this.path;
    }

    @Override
    public void movedTo(Path newPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void contentChanged() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(FSEntry otherFSEntry) {
        if ( otherFSEntry.isDirectory() ) {
            return 1;
        }
        else {
            return this.name.compareToIgnoreCase(otherFSEntry.name());
        }
    }

    @Override
    public Optional<Extension> extension() {
        return this.fs.extensions().getFor(this);
    }

    @Override
    public void open() {
        this.fs.open(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealFile)) return false;
        RealFile realFile = (RealFile) o;
        return path.equals(realFile.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
