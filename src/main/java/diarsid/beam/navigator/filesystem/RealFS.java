package diarsid.beam.navigator.filesystem;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import diarsid.beam.navigator.filesystem.ignoring.Ignores;

import static java.awt.Desktop.getDesktop;
import static java.nio.file.Files.exists;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.reverse;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

class RealFS implements FS {

    private final Ignores ignores;
    private final Function<Path, FSEntry> pathToFSEntry;
    private final HashMap<String, Directory> directoriesByPath;
    private final HashMap<String, File> filesByPath;
    private final MachineDirectory machineDirectory;
    private final Extensions extensions;
    private final Desktop desktop;
    private final Predicate<FSEntry> notIgnored;


    RealFS(
            Ignores ignores,
            HashMap<String, Directory> directoriesByPath,
            HashMap<String, File> filesByPath,
            FileSystem fileSystem) {
        this.ignores = ignores;
        this.directoriesByPath = directoriesByPath;
        this.filesByPath = filesByPath;
        this.machineDirectory = new MachineDirectory(this, fileSystem.getRootDirectories());
        this.extensions = new Extensions();
        this.desktop = getDesktop();

        this.pathToFSEntry = path -> {
            if (Files.isDirectory(path)) {
                return this.toDirectory(path);
            }
            else {
                return this.toFile(path);
            }
        };

        this.notIgnored = this.ignores::isNotIgnored;
    }

    @Override
    public Directory machineDirectory() {
        return this.machineDirectory;
    }

    @Override
    public FSEntry toFSEntry(Path path) {
        return this.pathToFSEntry.apply(path);
    }

    @Override
    public Directory toDirectory(Path path) {
        return this.directoriesByPath.computeIfAbsent(
                path.toAbsolutePath().toString(),
                fullName -> new RealDirectory(path, this));
    }

    @Override
    public File toFile(Path path) {
        return this.filesByPath.computeIfAbsent(
                path.toAbsolutePath().toString(),
                fullName -> new RealFile(path, this));
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
    public boolean copy(FSEntry whatToCopy, Directory whereToMove) {
        boolean success;

        if ( whatToCopy.isFile() ) {
            RealFile file = (RealFile) whatToCopy;
            try {
                Files.copy(file.nioPath(), whereToMove.nioPath().resolve(file.name()));
                success = true;
            }
            catch (IOException e) {
                this.handle(e);
                success = false;
            }
        }
        else {
            RealDirectory directoryToCopy = (RealDirectory) whatToCopy;
            RealDirectory directoryHost = (RealDirectory) whereToMove;

            Path directoryToCopyParentPath = directoryToCopy.nioPath().getParent();

            try {
                List<Path> paths = Files
                        .walk(directoryToCopy.nioPath())
                        .collect(toList());

                Path subPath;
                for (Path path : paths) {
                    subPath = directoryToCopyParentPath.relativize(path);
                    System.out.println(" copy ");
                    System.out.println(" src : " + path.toString());
                    System.out.println(" sub : " + subPath.toString());
                    System.out.println(" trg : " + directoryHost.nioPath().resolve(subPath).toString());
                    Files.copy(path, directoryHost.nioPath().resolve(subPath), COPY_ATTRIBUTES);

                }
                success = true;
            }
            catch (IOException e) {
                this.handle(e);
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean move(FSEntry whatToMove, Directory whereToMove) {
        boolean success;

        if ( whatToMove.isFile() ) {
            RealFile file = (RealFile) whatToMove;
            try {
                Files.move(file.nioPath(), whereToMove.nioPath().resolve(file.name()), REPLACE_EXISTING);
                success = true;
            }
            catch (IOException e) {
                this.handle(e);
                success = false;
            }
        }
        else {
            RealDirectory directoryToMove = (RealDirectory) whatToMove;
            RealDirectory directoryHost = (RealDirectory) whereToMove;

            try {
                Path oldPath = directoryToMove.nioPath();
                Path newPath = directoryHost.nioPath().resolve(directoryToMove.name());
                System.out.println(" move ");
                System.out.println(" old : " + oldPath);
                System.out.println(" new : " + newPath);
                Files.move(oldPath, newPath);

                this.directoriesByPath.remove(oldPath.toString());
                this.directoriesByPath.put(newPath.toString(), directoryToMove);

                Optional<Directory> previousParent = directoryToMove.parent();
                directoryToMove.movedTo(newPath);

                if ( previousParent.isPresent() ) {
                    previousParent.get().contentChanged();
                }

                directoryHost.contentChanged();

                success = true;
            }
            catch (IOException e) {
                this.handle(e);
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean remove(FSEntry entry) {
        boolean success;

        if ( entry.isFile() ) {
            RealFile file = (RealFile) entry;
            try {
                Files.delete(file.nioPath());
                success = true;
            }
            catch (IOException e) {
                this.handle(e);
                success = false;
            }
        }
        else {
            RealDirectory directoryToRemove = (RealDirectory) entry;

            try {
                List<Path> paths = Files.walk(directoryToRemove.nioPath())
                        .sorted(reverseOrder())
                        .collect(toList());

                for (Path path : paths) {
                    System.out.println(" del : " + path.toString());
                    Files.delete(path);
                }
                success = true;
            }
            catch (IOException e) {
                this.handle(e);
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean open(File file) {
        try {
            this.desktop.open(file.nioPath().toFile());
            return true;
        }
        catch (AccessDeniedException denied) {
            this.handle(denied);
            return false;
        }
        catch (IOException e) {
            this.handle(e);
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();;
            return false;
        }
    }

    @Override
    public Stream<FSEntry> list(Directory directory) {
        try {
             return Files
                    .list(directory.nioPath())
                    .map(this::toFSEntry)
                    .filter(this.notIgnored);
        }
        catch (AccessDeniedException denied) {
            this.handle(denied);
            return Stream.empty();
        }
        catch (IOException e) {
            this.handle(e);
            return Stream.empty();
        }
    }

    @Override
    public List<Directory> parentsOf(FSEntry fsEntry) {
        Path parentPath = fsEntry.nioPath().getParent();
        Directory parentDirectory;

        List<Directory> parents = new ArrayList<>();

        while ( nonNull(parentPath) && exists(parentPath) ) {
            parentDirectory = this.toDirectory(parentPath);
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
            size = Files.size(fsEntry.nioPath());
        }
        catch (IOException e) {
            this.handle(e);
            size = -1;
        }

        return size;
    }

    @Override
    public Extensions extensions() {
        return this.extensions;
    }

    private void handle(IOException e) {
        printStackTraceFor(e);
    }

    private void handle(AccessDeniedException e) {
        printStackTraceFor(e);
    }

    private void printStackTraceFor(IOException e) {
        System.out.println(e);
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().startsWith("diarsid")) {
                System.out.println("    " + element);
            }
        }
    }
}
