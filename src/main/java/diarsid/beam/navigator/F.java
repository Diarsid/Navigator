package diarsid.beam.navigator;

import java.nio.file.Paths;

import diarsid.beam.navigator.filesystem.Directory;
import diarsid.beam.navigator.filesystem.FS;

public class F {

    public static void main(String[] args) {
        move();
    }

    private static void iterateParents() {
        FS fs = FS.INSTANCE;
        Directory whatToIterare = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources"));
        whatToIterare.parents().forEach(parent -> System.out.println(parent.path()));
    }

    private static void copy() {
        FS fs = FS.INSTANCE;

        Directory whatToCopy = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/src"));
        Directory whereToCopy = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/trg"));

        fs.copy(whatToCopy, whereToCopy);
    }

    private static void delete() {
        FS fs = FS.INSTANCE;

        Directory whatToDelete = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/trg/src"));

        fs.remove(whatToDelete);
    }

    private static void move() {
        FS fs = FS.INSTANCE;

        Directory whatToMove = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/trg/src"));
        Directory whereToCopy = fs.toDirectory(Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources"));

        fs.move(whatToMove, whereToCopy);
    }
}
