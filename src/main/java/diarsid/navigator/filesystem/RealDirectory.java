package diarsid.navigator.filesystem;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import diarsid.support.objects.groups.Runnables;
import diarsid.support.objects.groups.Running;

import static java.nio.file.Files.list;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

class RealDirectory implements Directory {

    private final UUID uuid;
    private final FS fs;
    private final Runnables changeListeners;

    private Path path;
    private String name;
    private String fullName;

    RealDirectory(Path path, FS fs) {
        this.uuid = randomUUID();
        this.fs = fs;
        this.changeListeners = new Runnables();
        this.path = path.toAbsolutePath();
        this.name = getNameFrom(path);
        this.fullName = this.path.toString();
        System.out.println("created - Directory " + this.fullName);
    }

    private static String getNameFrom(Path path) {
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
            return Optional.of(this.fs.toDirectory(parent));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isParentOf(FSEntry fsEntry) {
        List<Directory> parents = fsEntry.parents();
        return parents.contains(this);
    }

    @Override
    public boolean isRoot() {
        return this.fs.isRoot(this);
    }

    @Override
    public List<Directory> parents() {
        return this.fs.parentsOf(this);
    }

    @Override
    public int depth() {
        return this.path.getNameCount();
    }

    @Override
    public void checkChildrenPresence(Consumer<Boolean> consumer) {
        try (Stream<Path> pathsStream = list(this.path)) {
            consumer.accept(pathsStream.count() > 0);
        }
        catch (AccessDeniedException denied) {
            consumer.accept(false);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void checkDirectoriesPresence(Consumer<Boolean> consumer) {
        try (Stream<Path> pathsStream = list(this.path)) {
            consumer.accept(pathsStream.anyMatch(this.fs::isDirectory));
        }
        catch (AccessDeniedException denied) {
            consumer.accept(false);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void checkFilesPresence(Consumer<Boolean> consumer) {
        try (Stream<Path> pathsStream = list(this.path)) {
            consumer.accept(pathsStream.anyMatch(this.fs::isFile));
        }
        catch (AccessDeniedException denied) {
            consumer.accept(false);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void feedChildren(Consumer<List<FSEntry>> consumer) {
        Stream<FSEntry> entriesStream = this.fs.list(this);

        List<FSEntry> entries = entriesStream
                .sorted()
                .collect(toList());

        entriesStream.close();

        consumer.accept(entries);
    }

    @Override
    public void feedDirectories(Consumer<List<Directory>> consumer) {
        Stream<FSEntry> entriesStream = this.fs.list(this);

        List<Directory> directories = entriesStream
                .filter(FSEntry::isDirectory)
                .map(FSEntry::asDirectory)
                .sorted()
                .collect(toList());

        entriesStream.close();

        consumer.accept(directories);
    }

    @Override
    public void feedFiles(Consumer<List<File>> consumer) {
        Stream<FSEntry> entriesStream = this.fs.list(this);

        List<File> files = entriesStream
                .filter(FSEntry::isFile)
                .map(FSEntry::asFile)
                .sorted()
                .collect(toList());

        entriesStream.close();

        consumer.accept(files);
    }

    @Override
    public void host(FSEntry newEntry, Consumer<Boolean> callback) {
        boolean result = this.fs.move(newEntry, this);
        callback.accept(result);
    }

    @Override
    public void hostAll(List<FSEntry> newEntries, Consumer<Boolean> callback, ProgressTracker<FSEntry> progressTracker) {
        boolean result = this.fs.moveAll(newEntries, this, progressTracker);
        callback.accept(result);
    }

    @Override
    public boolean host(FSEntry newEntry) {
        return this.fs.move(newEntry, this);
    }

    @Override
    public boolean hostAll(List<FSEntry> newEntries, ProgressTracker<FSEntry> progressTracker) {
        return this.fs.moveAll(newEntries, this, progressTracker);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
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
        return nonNull(this.path.getParent());
    }

    @Override
    public Running listenForChanges(Runnable listener) {
        return this.changeListeners.add(listener);
    }

    @Override
    public boolean canBe(Directory.Edit edit) {
        switch ( edit ) {
            case MOVED:
            case DELETED:
            case RENAMED:
                return ! this.fs.isRoot(this);
            case FILLED: return true;
            default: return false;
        }
    }

    @Override
    public int compareTo(FSEntry otherFSEntry) {
        if ( otherFSEntry.isFile() ) {
            return -1;
        }
        else {
            return this.name.compareToIgnoreCase(otherFSEntry.name());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealDirectory)) return false;
        RealDirectory directory = (RealDirectory) o;
        return uuid.equals(directory.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public Path nioPath() {
        return this.path;
    }

    @Override
    public void movedTo(Path newPath) {
        this.path = newPath;
        this.name = getNameFrom(this.path);
        this.fullName = this.path.toString();
        this.changeListeners.run();
    }

    @Override
    public void contentChanged() {
        this.changeListeners.run();
    }

    @Override
    public String toString() {
        return "RealDirectory{" +
                "fullName='" + fullName + '\'' +
                '}';
    }
}
