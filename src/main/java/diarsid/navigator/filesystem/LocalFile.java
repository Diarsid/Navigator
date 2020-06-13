package diarsid.navigator.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import diarsid.support.objects.groups.Runnables;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;

import static diarsid.navigator.filesystem.FileSystem.getNameFrom;

class LocalFile implements File, ChangeableFSEntry {

    private final UUID uuid;
    private final FileSystem fileSystem;
    private final Runnables contentChangeListeners;
    private final Runnables changeListeners;

    private Path path;
    private String name;
    private String fullName;

    LocalFile(Path path, FileSystem fileSystem) {
        this.uuid = randomUUID();
        this.path = path;
        this.name = path.getFileName().toString();
        this.fullName = path.toAbsolutePath().toString();
        this.fileSystem = fileSystem;
        fileSystem.isFile(this.path);
        this.contentChangeListeners = new Runnables();
        this.changeListeners = new Runnables();
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
    public long size() {
        return this.fileSystem.sizeOf(this);
    }

    @Override
    public void movedTo(Path newPath) {
        this.path = newPath;
        this.name = getNameFrom(this.path);
        this.fullName = this.path.toString();
    }

    @Override
    public void contentChanged() {
        this.contentChangeListeners.run();
    }

    @Override
    public void changed() {
        this.changeListeners.run();
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
        return uuid.equals(localFile.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
