package diarsid.navigator;

import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FS {

    public static void main(String[] args) throws Exception {
        FileSystem fileSystem = FileSystems.getDefault();

        Iterable<FileStore> stores = fileSystem.getFileStores();

        Iterable<Path> roots = fileSystem.getRootDirectories();

        for ( FileStore store : stores ) {
            System.out.println("store " + store.name() + " " + store.type() + " " + store);
        }

        for ( Path root : roots ) {
            System.out.println("root " + root);
            if ( root.toString().startsWith("E") ) {
                try ( Stream<Path> listing = Files.list(root) ) {
                    listing.forEach(path -> System.out.println(" - " + path.getFileName()));
                }
            }
        }
    }
}
