package diarsid.navigator.view.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.model.DirectoriesAtTabs;
import diarsid.navigator.model.DirectoryAtTab;
import diarsid.navigator.model.Tab;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

public class DirectoryAtTabTreeItems {

    private final DirectoriesAtTabs directoriesAtTabs;
    private final Map<DirectoryAtTab, DirectoryAtTabTreeItem> treeItemsByDirectory;
    private final BiFunction<Tab, Directory, DirectoryAtTabTreeItem> tabAndDirectoryToTreeItem;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemExpanded;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemCollapsed;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemCreated;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemRemoved;

    public DirectoryAtTabTreeItems(
            DirectoriesAtTabs directoriesAtTabs,
            Consumer<DirectoryAtTabTreeItem> onTreeItemExpanded,
            Consumer<DirectoryAtTabTreeItem> onTreeItemCollapsed,
            Consumer<DirectoryAtTabTreeItem> onTreeItemCreated,
            Consumer<DirectoryAtTabTreeItem> onTreeItemRemoved) {
        this.directoriesAtTabs = directoriesAtTabs;
        this.treeItemsByDirectory = new HashMap<>();
        this.tabAndDirectoryToTreeItem = (tab, directory) -> this.wrap(this.directoriesAtTabs.join(tab, directory));
        this.onTreeItemExpanded = onTreeItemExpanded;
        this.onTreeItemCollapsed = onTreeItemCollapsed;
        this.onTreeItemCreated = onTreeItemCreated;
        this.onTreeItemRemoved = onTreeItemRemoved;
    }

    public DirectoryAtTabTreeItem wrap(DirectoryAtTab directoryAtTab) {
        DirectoryAtTabTreeItem treeItem = this.treeItemsByDirectory.get(directoryAtTab);

        if ( isNull(treeItem) ) {
            treeItem = createNewFor(directoryAtTab);
            this.treeItemsByDirectory.put(directoryAtTab, treeItem);
            this.onTreeItemCreated.accept(treeItem);
        }

        return treeItem;
    }

    public List<DirectoryAtTabTreeItem> remove(Directory directory) {
        List<DirectoryAtTab> directoryAtTabs = this.directoriesAtTabs.getAllBy(directory);

        List<DirectoryAtTabTreeItem> removedItems = directoryAtTabs
                .stream()
                .map(this.treeItemsByDirectory::remove)
                .filter(Objects::nonNull)
                .collect(toList());

        removedItems.forEach(this.onTreeItemRemoved);

        return removedItems;
    }

    private DirectoryAtTabTreeItem createNewFor(DirectoryAtTab directoryAtTab) {
        return new DirectoryAtTabTreeItem(
                directoryAtTab, tabAndDirectoryToTreeItem, onTreeItemExpanded, onTreeItemCollapsed);
    }

    public Optional<DirectoryAtTabTreeItem> getExistedBy(DirectoryAtTab directoryAtTab) {
        return Optional.ofNullable(this.treeItemsByDirectory.get(directoryAtTab));
    }
}
