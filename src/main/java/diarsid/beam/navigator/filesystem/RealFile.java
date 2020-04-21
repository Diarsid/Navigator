package diarsid.beam.navigator.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class RealFile implements File {

    private final Path path;
    private final String name;
    private final String fullName;

    RealFile(Path path) {
        this.path = path;
        this.name = path.getFileName().toString();
        this.fullName = path.toAbsolutePath().toString();
        Files.isDirectory(this.path);
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
    public int compareTo(FSEntry otherFSEntry) {
        if ( otherFSEntry.isDirectory() ) {
            return 1;
        }
        else {
            return this.name.compareToIgnoreCase(otherFSEntry.name());
        }
    }
}
