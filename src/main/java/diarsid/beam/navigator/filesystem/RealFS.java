package diarsid.beam.navigator.filesystem;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Function;

class RealFS implements FS {

    private final Function<Path, FSEntry> pathToFSEntry;

    private final HashMap<String, Directory> directoriesByFullName;
    private final HashMap<String, File> filesByFullName;
    private final RootDirectory rootDirectory;

    RealFS(
            HashMap<String, Directory> directoriesByFullName,
            HashMap<String, File> filesByFullName,
            FileSystem fileSystem) {
        this.directoriesByFullName = directoriesByFullName;
        this.filesByFullName = filesByFullName;
        this.rootDirectory = new RootDirectory(this, fileSystem.getRootDirectories());

        pathToFSEntry = path -> {
            if (Files.isDirectory(path)) {
                return new RealDirectory(path, this);
            }
            else {
                return new RealFile(path);
            }
        };
    }

    @Override
    public Directory rootDirectory() {
        return this.rootDirectory;
    }

    @Override
    public FSEntry toFSEntry(Path path) {
        if (this.isDirectory(path)) {
            return this.toDirectory(path);
        }
        else {
            return this.toFile(path);
        }
    }

    @Override
    public Directory toDirectory(Path path) {
        return directoriesByFullName.computeIfAbsent(
                path.toAbsolutePath().toString(),
                fullName -> new RealDirectory(path, this));
    }

    @Override
    public File toFile(Path path) {
        return filesByFullName.computeIfAbsent(
                path.toAbsolutePath().toString(),
                fullName -> new RealFile(path));
    }

    @Override
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    @Override
    public boolean isFile(Path path) {
        return Files.isRegularFile(path);
    }
}
