package diarsid.navigator;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FS;
import diarsid.navigator.filesystem.FSEntry;

import static java.util.Collections.sort;

public class F {

    public static void main(String[] args) {
        sortDirs();
    }

    private static void iterateParents() {
        FS fs = FS.INSTANCE;
        Directory whatToIterare = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources"));
        whatToIterare.parents().forEach(parent -> System.out.println(parent.path()));
    }

    private static void copy() {
        FS fs = FS.INSTANCE;

        Directory whatToCopy = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources/src"));
        Directory whereToCopy = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources/trg"));

        fs.copy(whatToCopy, whereToCopy);
    }

    private static void delete() {
        FS fs = FS.INSTANCE;

        Directory whatToDelete = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources/trg/src"));

        fs.remove(whatToDelete);
    }

    private static void move() {
        FS fs = FS.INSTANCE;

        Directory whatToMove = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources/trg/src"));
        Directory whereToCopy = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/home/resources"));

        fs.move(whatToMove, whereToCopy);
    }

    private static void sortDirs() {
        FS fs = FS.INSTANCE;

        Directory d1 = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid"));
        Directory d2 = fs.toDirectory(Paths.get("D:/DEV/1__Projects"));
        Directory d3 = fs.toDirectory(Paths.get("D:/DEV/"));

        List<Directory> directories = new ArrayList<>();
        directories.add(d1);
        directories.add(d2);
        directories.add(d3);

        sort(directories, FSEntry.compareByDepth);

        System.out.println(directories);
    }
}
