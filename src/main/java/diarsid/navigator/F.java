package diarsid.navigator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FileSystem;
import diarsid.navigator.filesystem.FSEntry;

import static java.util.Collections.sort;

public class F {

    public static void main(String[] args) {
        rename();
    }

    private static void iterateParents() {
        FileSystem fileSystem = FileSystem.INSTANCE;
        Directory whatToIterare = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources")).get();
        whatToIterare.parents().forEach(parent -> System.out.println(parent.path()));
    }

    private static void copy() {
        FileSystem fileSystem = FileSystem.INSTANCE;

        Directory whatToCopy = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources/src")).get();
        Directory whereToCopy = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources/trg")).get();

        fileSystem.copy(whatToCopy, whereToCopy);
    }

    private static void delete() {
        FileSystem fileSystem = FileSystem.INSTANCE;

        Directory whatToDelete = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources/trg/src")).get();

        fileSystem.remove(whatToDelete);
    }

    private static void move() {
        FileSystem fileSystem = FileSystem.INSTANCE;

        Directory whatToMove = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources/trg/src")).get();
        Directory whereToCopy = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources")).get();

        fileSystem.move(whatToMove, whereToCopy);
    }

    private static void rename() {
        FileSystem fileSystem = FileSystem.INSTANCE;

        Directory whatToRename = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/test")).get();

        fileSystem.rename(whatToRename, "test_2");
    }

    private static void sortDirs() {
        FileSystem fileSystem = FileSystem.INSTANCE;

        Directory d1 = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid")).get();
        Directory d2 = fileSystem.toDirectory(Paths.get("D:/DEV/1__Projects")).get();
        Directory d3 = fileSystem.toDirectory(Paths.get("D:/DEV/")).get();

        List<Directory> directories = new ArrayList<>();
        directories.add(d1);
        directories.add(d2);
        directories.add(d3);

        sort(directories, FSEntry.compareByDepth);

        System.out.println(directories);
    }
}
