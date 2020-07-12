package diarsid.navigator.filesystem;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.file.Files.list;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.navigator.filesystem.FileSystem.getNameFrom;

class LocalDirectory implements Directory, ChangeableFSEntry {

    private final FileSystem fileSystem;

    private final Path path;
    private final String name;
    private final String fullName;

    LocalDirectory(Path path, FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.path = path.toAbsolutePath();
        this.name = getNameFrom(path);
        this.fullName = this.path.toString();
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
        return this.fileSystem.parentOf(this);
    }

    @Override
    public Optional<Directory> existedParent() {
        return this.fileSystem.existedParentOf(this.path);
    }

    @Override
    public boolean isIndirectParentOf(FSEntry fsEntry) {
        return fsEntry.path().startsWith(this.path) && ( ! this.equals(fsEntry) );
    }

    @Override
    public boolean isIndirectParentOf(Path path) {
        return path.startsWith(this.path) && ( ! this.path.equals(path) );
    }

    @Override
    public boolean isDirectParentOf(FSEntry fsEntry) {
        Path entryParent = fsEntry.path().getParent();

        if ( isNull(entryParent) ) {
            return false;
        }

        return entryParent.equals(this.path);
    }

    @Override
    public boolean isDirectParentOf(Path path) {
        Path entryParent = path.getParent();

        if ( isNull(entryParent) ) {
            return false;
        }

        return entryParent.equals(this.path);
    }

    @Override
    public boolean isRoot() {
        return this.fileSystem.isRoot(this);
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
            consumer.accept(pathsStream.anyMatch(this.fileSystem::isDirectory));
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
            consumer.accept(pathsStream.anyMatch(this.fileSystem::isFile));
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
        Stream<FSEntry> entriesStream = this.fileSystem.list(this);

        List<FSEntry> entries = entriesStream
                .sorted()
                .collect(toList());

        entriesStream.close();

        consumer.accept(entries);
    }

    @Override
    public void feedDirectories(Consumer<List<Directory>> consumer) {
        Stream<FSEntry> entriesStream = this.fileSystem.list(this);

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
        Stream<FSEntry> entriesStream = this.fileSystem.list(this);

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
        boolean result = this.fileSystem.move(newEntry, this);
        callback.accept(result);
    }

    @Override
    public void hostAll(
            List<FSEntry> newEntries,
            Consumer<Boolean> callback,
            ProgressTracker<FSEntry> progressTracker) {
        boolean result = this.fileSystem.moveAll(newEntries, this, progressTracker);
        callback.accept(result);
    }

    @Override
    public boolean host(FSEntry newEntry) {
        return this.fileSystem.move(newEntry, this);
    }

    @Override
    public boolean hostAll(List<FSEntry> newEntries, ProgressTracker<FSEntry> progressTracker) {
        return this.fileSystem.moveAll(newEntries, this, progressTracker);
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
        return this.fileSystem.move(this, newPlace);
    }

    @Override
    public boolean remove() {
        return this.fileSystem.remove(this);
    }

    @Override
    public boolean canBeIgnored() {
        return nonNull(this.path.getParent());
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
    public boolean canBe(Directory.Edit edit) {
        switch ( edit ) {
            case MOVED:
            case DELETED:
            case RENAMED:
                return ! this.fileSystem.isRoot(this);
            case FILLED: return true;
            default: return false;
        }
    }

    @Override
    public void watch() {
        this.fileSystem.watch(this);
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
        if (!(o instanceof LocalDirectory)) return false;
        LocalDirectory that = (LocalDirectory) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "LocalDirectory{" +
                "fullName='" + fullName + '\'' +
                '}';
    }
}
