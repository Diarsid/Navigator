package diarsid.beam.navigator.model;

import java.util.HashMap;
import java.util.Map;

import diarsid.beam.navigator.filesystem.Directory;

import static java.util.Objects.isNull;

public class DirectoriesAtTabs {

    private final Map<Tab, Map<Directory, DirectoryAtTab>> directoriesAtTabsByTabs;

    public DirectoriesAtTabs(Map<Tab, Map<Directory, DirectoryAtTab>> directoriesAtTabsByTabs) {
        this.directoriesAtTabsByTabs = directoriesAtTabsByTabs;
    }

    public DirectoryAtTab join(Tab tab, Directory directory) {
        Map<Directory, DirectoryAtTab> directoriesAtTabs = directoriesAtTabsByTabs.get(tab);

        if (isNull(directoriesAtTabs)) {
            directoriesAtTabs = new HashMap<>();
            directoriesAtTabsByTabs.put(tab, directoriesAtTabs);
        }

        DirectoryAtTab directoryAtTab = directoriesAtTabs.get(directory);

        if (isNull(directoryAtTab)) {
            directoryAtTab = new DirectoryAtTab(tab, directory);
            directoriesAtTabs.put(directory, directoryAtTab);
        }

        return directoryAtTab;
    }
}
