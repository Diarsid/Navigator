package diarsid.beam.navigator.filesystem;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.nio.file.Files.list;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

class RealDirectory implements Directory {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    private final Path path;
    private final String name;
    private final String fullName;
    private final FS fs;

    RealDirectory(Path path, FS fs) {
        this.path = path;
        this.fs = fs;
        Path fileName = path.getFileName();
        if (isNull(fileName)) {
            this.name = path.toString();
        }
        else {
            this.name = fileName.toString();
        }
        this.fullName = this.path.toAbsolutePath().toString();
        System.out.println(format("%s:%s created - %s", this.getClass().getSimpleName(), COUNTER.incrementAndGet(), fullName));
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String fullName() {
        return this.fullName;
    }

    @Override
    public void checkChildrenPresence(Consumer<Boolean> consumer) {
        try {
            consumer.accept(list(this.path).count() > 0);
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
        try {
            consumer.accept(list(this.path).anyMatch(fs::isDirectory));
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
        try {
            consumer.accept(list(this.path).anyMatch(fs::isFile));
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
        try {
            List<FSEntry> entries = list(this.path)
                    .map(fs::toFSEntry)
                    .sorted()
                    .collect(toList());

            consumer.accept(entries);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void feedDirectories(Consumer<List<Directory>> consumer) {
        try {
            List<Directory> directories = list(this.path)
                    .filter(fs::isDirectory)
                    .map(fs::toDirectory)
                    .sorted()
                    .collect(toList());

            consumer.accept(directories);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void feedFiles(Consumer<List<File>> consumer) {
        try {
            List<File> files = list(this.path)
                    .filter(fs::isFile)
                    .map(fs::toFile)
                    .sorted()
                    .collect(toList());

            consumer.accept(files);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
        RealDirectory that = (RealDirectory) o;
        return this.fullName.equals(that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fullName);
    }
}
