package diarsid.beam.navigator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import diarsid.beam.navigator.filesystem.Directory;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class DirectoriesAtTabs {

    private final Map<Directory, List<DirectoryAtTab>> directoryAtTabsListByDirectories;
    private final Map<Tab, Map<Directory, DirectoryAtTab>> directoriesAtTabsByTabs;

    public DirectoriesAtTabs() {
        this.directoryAtTabsListByDirectories = new HashMap<>();
        this.directoriesAtTabsByTabs = new HashMap<>();
    }

    public DirectoryAtTab join(Tab tab, Directory directory) {
        Map<Directory, DirectoryAtTab> directoriesAtTabs = this.directoriesAtTabsByTabs.get(tab);

        if ( isNull(directoriesAtTabs) ) {
            directoriesAtTabs = new HashMap<>();
            this.directoriesAtTabsByTabs.put(tab, directoriesAtTabs);
        }

        DirectoryAtTab directoryAtTab = directoriesAtTabs.get(directory);

        if ( isNull(directoryAtTab) ) {
            directoryAtTab = new DirectoryAtTab(tab, directory);
            directoriesAtTabs.put(directory, directoryAtTab);

            List<DirectoryAtTab> directoryAtTabs = this.directoryAtTabsListByDirectories.get(directory);

            if ( isNull(directoryAtTabs) ) {
                directoryAtTabs = new ArrayList<>();
                this.directoryAtTabsListByDirectories.put(directory, directoryAtTabs);
            }

            directoryAtTabs.add(directoryAtTab);
        }

        return directoryAtTab;
    }

    public Optional<DirectoryAtTab> getBy(Tab tab, Directory directory) {
        DirectoryAtTab directoryAtTab = null;

        Map<Directory, DirectoryAtTab> directoriesAtTabsByTab = this.directoriesAtTabsByTabs.get(tab);
        if ( nonNull(directoriesAtTabsByTab) ) {
            directoryAtTab = directoriesAtTabsByTab.get(directory);
        }

        return Optional.ofNullable(directoryAtTab);
    }

    public List<DirectoryAtTab> getAllBy(Directory directory) {
        List<DirectoryAtTab> directoryAtTabs = this.directoryAtTabsListByDirectories.get(directory);

        if ( isNull(directoryAtTabs) ) {
            directoryAtTabs = emptyList();
        }

        return directoryAtTabs;
    }
}
