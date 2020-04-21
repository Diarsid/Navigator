package diarsid.beam.navigator.view.filetree;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import diarsid.beam.navigator.filesystem.Directory;
import diarsid.beam.navigator.model.DirectoryAtTab;
import diarsid.beam.navigator.model.Tab;

public class DirectoryAtTabTreeItems {

    private final HashMap<DirectoryAtTab, DirectoryAtTabTreeItem> treeItemsByDirectory;
    private final BiFunction<Tab, Directory, DirectoryAtTab> joinTabAndDirectory;
    private final BiFunction<Tab, Directory, DirectoryAtTabTreeItem> directoryToTreeItem;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemExpanded;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemCollapsed;

    public DirectoryAtTabTreeItems(
            HashMap<DirectoryAtTab, DirectoryAtTabTreeItem> treeItemsByDirectory,
            BiFunction<Tab, Directory, DirectoryAtTab> joinDirectoryAndTab,
            Consumer<DirectoryAtTabTreeItem> onTreeItemExpanded,
            Consumer<DirectoryAtTabTreeItem> onTreeItemCollapsed) {
        this.treeItemsByDirectory = treeItemsByDirectory;
        this.joinTabAndDirectory = joinDirectoryAndTab;
        this.directoryToTreeItem = (tab, directory) -> this.wrap(this.joinTabAndDirectory.apply(tab, directory));
        this.onTreeItemExpanded = onTreeItemExpanded;
        this.onTreeItemCollapsed = onTreeItemCollapsed;
    }

    public DirectoryAtTabTreeItem wrap(DirectoryAtTab directoryAtTab) {
        return treeItemsByDirectory.computeIfAbsent(
                directoryAtTab,
                fullName -> new DirectoryAtTabTreeItem(
                        directoryAtTab, directoryToTreeItem, onTreeItemExpanded, onTreeItemCollapsed));
    }
}
