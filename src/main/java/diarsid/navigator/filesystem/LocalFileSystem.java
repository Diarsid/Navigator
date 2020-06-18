package diarsid.navigator.filesystem;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import diarsid.navigator.filesystem.ignoring.Ignores;

import static java.awt.Desktop.getDesktop;
import static java.nio.file.Files.exists;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.reverse;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.navigator.filesystem.FileSystemType.LOCAL;

class LocalFileSystem implements FileSystem {

    static class PathMove {

        private Path oldPath;
        private Path newPath;


        PathMove() {
        }

        void setFromTo(Path from, Path to) {
            this.oldPath = from;
            this.newPath = to;
        }

        List<PathChange> findChangesTo(List<Path> oldPaths) {
            List<PathChange> changes = oldPaths
                    .stream()
                    .map(this::findChangeTo)
                    .filter(Objects::nonNull)
                    .collect(toList());

            return changes;
        }

        private PathChange findChangeTo(Path removedPath) {
            Path removedPathRelative = this.oldPath.relativize(removedPath);
            try {
                Path createdPath = this.newPath.resolve(removedPathRelative).toRealPath();
                return new PathChange(removedPath, createdPath, removedPathRelative);
            } catch (IOException e) {
                handle(e);
                return null;
            }
        }

        void clear() {
            this.oldPath = null;
            this.newPath = null;
        }
    }

    static class PathChange {

        private final Path oldPath;
        private final Path newPath;
        private final Path relativePath;
        private final boolean isDirectory;

        PathChange(Path oldPath, Path newPath, Path relativePath) {
            this.oldPath = oldPath;
            this.newPath = newPath;
            this.relativePath = relativePath;
            this.isDirectory = Files.exists(newPath) && Files.isDirectory(newPath);
        }

        public Path oldPath() {
            return oldPath;
        }

        public Path newPath() {
            return newPath;
        }

        public Path relativePath() {
            return relativePath;
        }

        public boolean isDirectory() {
            return isDirectory;
        }

        @Override
        public String toString() {
            return "PathChange{" +
                    "old=" + oldPath +
                    ", new=" + newPath +
                    ", relative=" + relativePath +
                    ", type=" + (isDirectory ? "directory" : "file") +
                    '}';
        }
    }

    private final Ignores ignores;
    private final HashMap<Path, LocalDirectory> directoriesByPath;
    private final HashMap<Path, LocalFile> filesByPath;
    private final LocalMachineDirectory localMachineDirectory;
    private final Extensions extensions;
    private final Desktop desktop;
    private final Predicate<FSEntry> notIgnored;
    private final PathMove pathMove;

    LocalFileSystem(
            Ignores ignores,
            java.nio.file.FileSystem fileSystem) {
        this.ignores = ignores;
        this.directoriesByPath = new HashMap<>();
        this.filesByPath = new HashMap<>();
        this.localMachineDirectory = new LocalMachineDirectory(this, fileSystem.getRootDirectories());
        this.extensions = new Extensions();
        this.desktop = getDesktop();
        this.notIgnored = this.ignores::isNotIgnored;
        this.pathMove = new PathMove();
    }

    @Override
    public Directory machineDirectory() {
        return this.localMachineDirectory;
    }

    @Override
    public Optional<FSEntry> toFSEntry(Path path) {
        if ( Files.exists(path) ) {
            try {
                Path realPath = path.toRealPath();
                return Optional.of(this.toLocalFSEntry(realPath));
            }
            catch (IOException e) {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }

    FSEntry toLocalFSEntry(Path path) {
        if (Files.isDirectory(path)) {
            return this.toLocalDirectory(path);
        }
        else {
            return this.toLocalFile(path);
        }
    }

    @Override
    public Optional<Directory> toDirectory(Path path) {
        if ( Files.exists(path) ) {
            if ( Files.isDirectory(path) ) {
                try {
                    Path realPath = path.toRealPath();
                    return Optional.of(this.toLocalDirectory(realPath));
                }
                catch (IOException e) {
                    return Optional.empty();
                }
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }

    LocalDirectory toLocalDirectory(Path path) {
        return this.directoriesByPath.computeIfAbsent(
                path.toAbsolutePath(),
                fullName -> this.createNewLocalDirectory(path));
    }

    private LocalDirectory createNewLocalDirectory(Path path) {
        LocalDirectory localDirectory = new LocalDirectory(path, this);
        return localDirectory;
    }

    @Override
    public Optional<File> toFile(Path path) {
        if ( Files.exists(path) ) {
            if ( ! Files.isDirectory(path) ) {
                try {
                    Path realPath = path.toRealPath();
                    return Optional.of(this.toLocalFile(realPath));
                }
                catch (IOException e) {
                    return Optional.empty();
                }
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }

    LocalFile toLocalFile(Path path) {
        return this.filesByPath.computeIfAbsent(
                path.toAbsolutePath(),
                fullName -> new LocalFile(path, this));
    }

    @Override
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    @Override
    public boolean isFile(Path path) {
        return Files.isRegularFile(path);
    }

    @Override
    public boolean copy(FSEntry whatToCopy, Directory whereToCopy) {
        boolean success;

        LocalDirectory directoryHost = (LocalDirectory) whereToCopy;
        if ( whatToCopy.isFile() ) {
            LocalFile file = (LocalFile) whatToCopy;
            try {
                Files.copy(file.path(), directoryHost.path().resolve(file.name()));
                success = true;
            }
            catch (IOException e) {
                handle(e);
                success = false;
            }
        }
        else {
            LocalDirectory directoryToCopy = (LocalDirectory) whatToCopy;

            if ( directoryToCopy.equals(directoryHost) ) {
                return false;
            }

            Path directoryToCopyParentPath = directoryToCopy.path().getParent();

            try {
                List<Path> paths = Files
                        .walk(directoryToCopy.path())
                        .collect(toList());

                Path subPath;
                for (Path path : paths) {
                    subPath = directoryToCopyParentPath.relativize(path);
                    System.out.println(" copy ");
                    System.out.println(" src : " + path.toString());
                    System.out.println(" sub : " + subPath.toString());
                    System.out.println(" trg : " + directoryHost.path().resolve(subPath).toString());
                    Files.copy(path, directoryHost.path().resolve(subPath), COPY_ATTRIBUTES);

                }
                success = true;
            }
            catch (IOException e) {
                handle(e);
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean move(FSEntry whatToMove, Directory whereToMove) {
        boolean success;

        LocalDirectory directoryHost = (LocalDirectory) whereToMove;

        if ( whatToMove.isFile() ) {
            LocalFile fileToMove = (LocalFile) whatToMove;
            try {
                Path oldPath = fileToMove.path();
                Path newPath = directoryHost.path().resolve(fileToMove.name());

                Optional<Directory> previousParent = fileToMove.parent();

                Files.move(fileToMove.path(), directoryHost.path().resolve(fileToMove.name()), REPLACE_EXISTING);

                this.filesByPath.remove(oldPath);
                this.filesByPath.put(newPath, fileToMove);

                fileToMove.movedTo(directoryHost.path());

                if ( previousParent.isPresent() ) {
                    LocalDirectory previousParentDirectory = (LocalDirectory) previousParent.get();
                    previousParentDirectory.contentChanged();
                }

                directoryHost.contentChanged();

                fileToMove.changed();

                success = true;
            }
            catch (IOException e) {
                handle(e);
                success = false;
            }
        }
        else {
            LocalDirectory directoryToMove = (LocalDirectory) whatToMove;
            if ( directoryToMove.equals(directoryHost) ) {
                return false;
            }

            if ( directoryToMove.isParentOf(directoryHost) ) {
                return false;
            }

            try {
                Path oldPath = directoryToMove.path();
                Path newPath = directoryHost.path().resolve(directoryToMove.name());
                this.pathMove.setFromTo(oldPath, newPath);
                System.out.println("[FS] [move] " + oldPath + " -> " + newPath);

                List<Path> removedPaths = Files.walk(oldPath).collect(toList());

                Files.move(oldPath, newPath);

                List<PathChange> pathChanges = this.pathMove.findChangesTo(removedPaths);
                this.pathMove.clear();

                Optional<Directory> previousParent = directoryToMove.parent();

                List<ChangeableFSEntry> movedEntries = pathChanges
                        .stream()
                        .map(this::makeChangeToCachedPaths)
                        .filter(Objects::nonNull)
                        .collect(toList());

                if ( previousParent.isPresent() ) {
                    LocalDirectory previousParentDirectory = (LocalDirectory) previousParent.get();
                    previousParentDirectory.contentChanged();
                }

                directoryHost.contentChanged();

                movedEntries.forEach(ChangeableFSEntry::changed);

                success = true;
            }
            catch (IOException e) {
                handle(e);
                success = false;
            }
        }

        return success;
    }

    private ChangeableFSEntry makeChangeToCachedPaths(PathChange pathChange) {
        if ( pathChange.isDirectory() ) {
            LocalDirectory directory = this.directoriesByPath.remove(pathChange.oldPath);

            if ( nonNull(directory) ) {
                directory.movedTo(pathChange.newPath);
                this.directoriesByPath.put(pathChange.newPath, directory);
                return directory;
            }
            else {
                return null;
            }
        }
        else {
            LocalFile file = this.filesByPath.remove(pathChange.oldPath);

            if ( nonNull(file) ) {
                file.movedTo(pathChange.newPath);
                this.filesByPath.put(pathChange.newPath, file);
                return file;
            }
            else {
                return null;
            }
        }
    }


    @Override
    public boolean remove(FSEntry entry) {
        boolean success;

        if ( entry.isFile() ) {
            LocalFile file = (LocalFile) entry;
            try {
                Files.delete(file.path());
                success = true;
            }
            catch (IOException e) {
                handle(e);
                success = false;
            }
        }
        else {
            LocalDirectory directoryToRemove = (LocalDirectory) entry;

            try {
                List<Path> paths = Files.walk(directoryToRemove.path())
                        .sorted(reverseOrder())
                        .collect(toList());

                // TODO !!!!

                for (Path path : paths) {
                    System.out.println(" del : " + path.toString());
                    Files.delete(path);
                }
                success = true;
            }
            catch (IOException e) {
                handle(e);
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean moveAll(
            List<FSEntry> whatToMove,
            Directory parentDirectoryWhereToMove,
            ProgressTracker<FSEntry> progressTracker) {
        progressTracker.begin(whatToMove.size());

        whatToMove.sort(FSEntry.compareByDepth);

        boolean moved;
        for (FSEntry entry : whatToMove) {
            progressTracker.processing(entry);
            moved = this.move(entry, parentDirectoryWhereToMove);
            if ( moved ) {
                progressTracker.processingDone(entry);
            }
            else {

            }
        }

        progressTracker.completed();
        return true;
    }

    @Override
    public boolean copyAll(
            List<FSEntry> whatToCopy,
            Directory parentDirectoryWhereToCopy,
            ProgressTracker<FSEntry> progressTracker) {
        progressTracker.begin(whatToCopy.size());

        whatToCopy.sort(FSEntry.compareByDepth);

        for (FSEntry entry : whatToCopy) {
            progressTracker.processing(entry);
            this.copy(entry, parentDirectoryWhereToCopy);
            progressTracker.processingDone(entry);
        }

        progressTracker.completed();
        return true;
    }

    @Override
    public boolean removeAll(List<FSEntry> whatToRemove, ProgressTracker<FSEntry> progressTracker) {
        progressTracker.begin(whatToRemove.size());

        whatToRemove.sort(FSEntry.compareByDepth);

        for (FSEntry entry : whatToRemove) {
            progressTracker.processing(entry);
            this.remove(entry);
            progressTracker.processingDone(entry);
        }

        progressTracker.completed();
        return true;
    }

    @Override
    public boolean open(File file) {
        LocalFile localFile = (LocalFile) file;
        try {
            this.desktop.open(localFile.path().toFile());
            return true;
        }
        catch (AccessDeniedException denied) {
            handle(denied);
            return false;
        }
        catch (IOException e) {
            handle(e);
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();;
            return false;
        }
    }

    @Override
    public Stream<FSEntry> list(Directory directory) {
        LocalDirectory localDirectory = (LocalDirectory) directory;
        try {
             return Files
                     .list(localDirectory.path())
                     .map(this::toFSEntry)
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .filter(this.notIgnored);
        }
        catch (AccessDeniedException denied) {
            handle(denied);
            return Stream.empty();
        }
        catch (IOException e) {
            handle(e);
            return Stream.empty();
        }
    }

    @Override
    public Optional<Directory> parentOf(FSEntry fsEntry) {
        Path parentPath = fsEntry.path().getParent();
        if ( nonNull(parentPath) ) {
            return this.toDirectory(parentPath);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public List<Directory> parentsOf(FSEntry fsEntry) {
        Path parentPath = fsEntry.path().getParent();
        Directory parentDirectory;

        List<Directory> parents = new ArrayList<>();

        while ( nonNull(parentPath) && exists(parentPath) ) {
            parentDirectory = this.toLocalDirectory(parentPath);
            parents.add(parentDirectory);
            parentPath = parentPath.getParent();
        }

        reverse(parents);

        return parents;
    }

    @Override
    public long sizeOf(FSEntry fsEntry) {
        long size;

        try {
            size = Files.size(fsEntry.path());
        }
        catch (IOException e) {
            handle(e);
            size = -1;
        }

        return size;
    }

    @Override
    public Extensions extensions() {
        return this.extensions;
    }

    @Override
    public boolean isRoot(Directory directory) {
        LocalDirectory localDirectory = (LocalDirectory) directory;
        return this.localMachineDirectory.roots().contains(localDirectory.path());
    }

    @Override
    public boolean isMachine(Directory directory) {
        return directory instanceof LocalMachineDirectory;
    }

    @Override
    public FileSystemType type() {
        return LOCAL;
    }

    private static void handle(IOException e) {
        printStackTraceFor(e);
    }

    private static void handle(AccessDeniedException e) {
        printStackTraceFor(e);
    }

    private static void printStackTraceFor(IOException e) {
        System.out.println(e);
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().startsWith("diarsid")) {
                System.out.println("    " + element);
            }
        }
    }
}
