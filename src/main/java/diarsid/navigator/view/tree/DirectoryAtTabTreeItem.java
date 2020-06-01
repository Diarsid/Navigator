package diarsid.navigator.view.tree;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javafx.scene.control.TreeItem;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.model.DirectoryAtTab;
import diarsid.navigator.model.Tab;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class DirectoryAtTabTreeItem extends TreeItem<String> implements Comparable<DirectoryAtTabTreeItem> {

    private final static TreeItem<String> PLACEHOLDER = new TreeItem<>("...");
    private final Tab tab;
    private final Directory directory;
    private final DirectoryAtTab directoryAtTab;
    private final BiFunction<Tab, Directory, DirectoryAtTabTreeItem> directoryToTreeItem;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemExpanded;
    private final Consumer<DirectoryAtTabTreeItem> onTreeItemCollapsed;

    DirectoryAtTabTreeItem(
            DirectoryAtTab directoryAtTab,
            BiFunction<Tab, Directory, DirectoryAtTabTreeItem> directoryToTreeItem,
            Consumer<DirectoryAtTabTreeItem> onTreeItemExpanded,
            Consumer<DirectoryAtTabTreeItem> onTreeItemCollapsed) {
        super(directoryAtTab.directory().name());
        this.tab = directoryAtTab.tab();
        this.directory = directoryAtTab.directory();
        this.directoryAtTab = directoryAtTab;
        this.directoryToTreeItem = directoryToTreeItem;
        this.onTreeItemExpanded = onTreeItemExpanded;
        this.onTreeItemCollapsed = onTreeItemCollapsed;

        this.directory.listenForChanges(this::initializeItemOrSetPlaceholder);

        this.setPlaceholderIfChildrenPresent();

        super.addEventHandler(TreeItem.branchExpandedEvent(), (TreeItem.TreeModificationEvent<String> event) -> {
            initializeItem();
            event.consume();
            this.tab.selectedDirectory().ifPresent(directory -> this.onTreeItemExpanded.accept(this));
        });

        super.addEventHandler(TreeItem.branchCollapsedEvent(), (TreeItem.TreeModificationEvent<String> event) -> {
            super.getChildren().clear();
            super.getChildren().add(PLACEHOLDER);
            this.tab.selectedDirectory().ifPresent(directory -> this.onTreeItemCollapsed.accept(this));
            event.consume();
        });

        System.out.println(format("created - %s %s", this.getClass().getSimpleName(), this.directory.path()));
    }

    private void initializeItemOrSetPlaceholder() {
        if ( super.expandedProperty().get() ) {
            this.initializeItem();
        }
        else {
            this.setPlaceholderIfChildrenPresent();
        }
    }

    private void initializeItem() {
        super.getChildren().clear();

        this.directory.feedDirectories((directories) -> {
            List<TreeItem<String>> items = directories
                    .stream()
                    .filter(Directory::isNotHidden)
                    .map(directory -> this.directoryToTreeItem.apply(this.tab, directory))
                    .sorted()
                    .peek(DirectoryAtTabTreeItem::initializeItemOrSetPlaceholder)
                    .collect(toList());

            super.getChildren().setAll(items);
        });
    }

    public void setSelectedToTab() {
        this.tab.selectedDirectory().resetTo(this.directoryAtTab);
    }

    public Tab tab() {
        return this.tab;
    }

    public Directory directory() {
        return this.directory;
    }

    private void setPlaceholderIfChildrenPresent() {
        this.directory.checkDirectoriesPresence(this::setPlaceholderIfChildrenPresent);

    }

    private void setPlaceholderIfChildrenPresent(boolean childrenPresent) {
        super.getChildren().clear();
        if ( childrenPresent ) {
            super.getChildren().add(PLACEHOLDER);
        }
    }

    @Override
    public int compareTo(DirectoryAtTabTreeItem other) {
        return this.directory.compareTo(other.directory);
    }

//    public DirectoryAtTab directoryAtTab() {
//        return this.directoryAtTab;
//    }
//
//    public DirectoryAtTabTreeItem getInChildren(Directory directory) {
//        return super
//                .getChildren()
//                .stream()
//                .map(treeItem -> (DirectoryAtTabTreeItem) treeItem)
//                .filter(treeItem -> treeItem.directory().name().equalsIgnoreCase(directory.name()))
//                .findFirst()
//                .orElseThrow();
//    }

}
