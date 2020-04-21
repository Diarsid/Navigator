package diarsid.beam.navigator.filesystem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

class RootDirectory implements Directory {

    private final FS fs;
    private final List<Path> roots;

    RootDirectory(FS fs, Iterable<Path> rootPaths) {
        this.fs = fs;
        this.roots = new ArrayList<>();
        rootPaths.forEach(this.roots::add);
    }

    @Override
    public String name() {
        return "/";
    }

    @Override
    public String fullName() {
        return "/";
    }

    @Override
    public void checkChildrenPresence(Consumer<Boolean> consumer) {
        consumer.accept(true);
    }

    @Override
    public void checkDirectoriesPresence(Consumer<Boolean> consumer) {
        consumer.accept(true);
    }

    @Override
    public void checkFilesPresence(Consumer<Boolean> consumer) {
        consumer.accept(false);
    }

    @Override
    public void feedChildren(Consumer<List<FSEntry>> consumer) {
        List<FSEntry> entries = this.roots
                .stream()
                .map(this.fs::toFSEntry)
                .collect(toList());

        consumer.accept(entries);
    }

    @Override
    public void feedDirectories(Consumer<List<Directory>> consumer) {
        List<Directory> directories = this.roots
                .stream()
                .filter(this.fs::isDirectory)
                .map(this.fs::toDirectory)
                .collect(toList());

        consumer.accept(directories);
    }

    @Override
    public void feedFiles(Consumer<List<File>> consumer) {
        List<File> files = this.roots
                .stream()
                .filter(this.fs::isFile)
                .map(this.fs::toFile)
                .collect(toList());

        consumer.accept(files);
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
        return false;
    }

    @Override
    public int compareTo(FSEntry otherFSEntry) {
        return 0;
    }
}
