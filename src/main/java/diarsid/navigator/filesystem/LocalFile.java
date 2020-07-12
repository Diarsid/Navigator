package diarsid.navigator.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;

class LocalFile implements File, ChangeableFSEntry {

    private final FileSystem fileSystem;

    private final Path path;
    private final String name;
    private final String fullName;

    LocalFile(Path path, FileSystem fileSystem) {
        this.path = path;
        this.name = path.getFileName().toString();
        this.fullName = path.toAbsolutePath().toString();
        this.fileSystem = fileSystem;
        fileSystem.isFile(this.path);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Path path() {
        return this.path;
    }

    @Override
    public void showInDefaultFileManager() {
        this.fileSystem.showInDefaultFileManager(this);
    }

    @Override
    public Optional<Directory> parent() {
        Path parent = this.path.getParent();
        if ( nonNull(parent) ) {
            return this.fileSystem.toDirectory(parent);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Directory> existedParent() {
        return this.fileSystem.existedParentOf(this.path);
    }

    @Override
    public List<Directory> parents() {
        return this.fileSystem.parentsOf(this);
    }

    @Override
    public int depth() {
        return this.path.getNameCount();
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
        return this.fileSystem.move(this, newPlace);
    }

    @Override
    public boolean remove() {
        return this.fileSystem.remove(this);
    }

    @Override
    public boolean canBeIgnored() {
        return true;
    }

    @Override
    public boolean exists() {
        return this.fileSystem.exists(this);
    }

    @Override
    public boolean isAbsent() {
        return this.fileSystem.isAbsent(this);
    }

    @Override
    public FileSystem fileSystem() {
        return this.fileSystem;
    }

    @Override
    public long size() {
        return this.fileSystem.sizeOf(this);
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
        return this.fileSystem.extensions().getFor(this);
    }

    @Override
    public void open() {
        this.fileSystem.open(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalFile)) return false;
        LocalFile localFile = (LocalFile) o;
        return path.equals(localFile.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
