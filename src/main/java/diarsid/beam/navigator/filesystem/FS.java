package diarsid.beam.navigator.filesystem;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;

public interface FS {

    FS INSTANCE = new RealFS(new HashMap<>(), new HashMap<>(), FileSystems.getDefault());

    Directory rootDirectory();

    FSEntry toFSEntry(Path path);

    Directory toDirectory(Path path);

    File toFile(Path path);

    boolean isDirectory(Path path);

    boolean isFile(Path path);

}
