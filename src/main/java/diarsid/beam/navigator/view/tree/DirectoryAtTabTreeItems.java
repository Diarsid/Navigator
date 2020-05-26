package diarsid.beam.navigator.view.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import diarsid.beam.navigator.filesystem.Directory;
import diarsid.beam.navigator.model.DirectoriesAtTabs;
import diarsid.beam.navigator.model.DirectoryAtTab;
import diarsid.beam.navigator.model.Tab;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

public class DirectoryAtTabTreeItems {

    private final DirectoriesAtTabs directoriesAtTabs;
    private final Map<DirectoryAtTab, DirectoryAtTabTreeItem> treeItemsByDirectory;
    private final BiFunction<Tab, Directory, DirectoryAtTabTreeItem> tabAndDirectoryToTreeItem;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemExpanded;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemCollapsed;

    public DirectoryAtTabTreeItems(
            DirectoriesAtTabs directoriesAtTabs,
            Consumer<DirectoryAtTabTreeItem> onTreeItemExpanded,
            Consumer<DirectoryAtTabTreeItem> onTreeItemCollapsed) {
        this.directoriesAtTabs = directoriesAtTabs;
        this.treeItemsByDirectory = new HashMap<>();
        this.tabAndDirectoryToTreeItem = (tab, directory) -> this.wrap(this.directoriesAtTabs.join(tab, directory));
        this.onTreeItemExpanded = onTreeItemExpanded;
        this.onTreeItemCollapsed = onTreeItemCollapsed;
    }

    public DirectoryAtTabTreeItem wrap(DirectoryAtTab directoryAtTab) {
        DirectoryAtTabTreeItem treeItem = this.treeItemsByDirectory.get(directoryAtTab);

        if ( isNull(treeItem) ) {
            treeItem = createNewFor(directoryAtTab);
            this.treeItemsByDirectory.put(directoryAtTab, treeItem);
        }

        return treeItem;
    }

    public List<DirectoryAtTabTreeItem> remove(Directory directory) {
        List<DirectoryAtTab> directoryAtTabs = this.directoriesAtTabs.getAllBy(directory);

        return directoryAtTabs
                .stream()
                .map(this.treeItemsByDirectory::remove)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private DirectoryAtTabTreeItem createNewFor(DirectoryAtTab directoryAtTab) {
        return new DirectoryAtTabTreeItem(
                directoryAtTab, tabAndDirectoryToTreeItem, onTreeItemExpanded, onTreeItemCollapsed);
    }

    public Optional<DirectoryAtTabTreeItem> getExistedBy(DirectoryAtTab directoryAtTab) {
        return Optional.ofNullable(this.treeItemsByDirectory.get(directoryAtTab));
    }
}
