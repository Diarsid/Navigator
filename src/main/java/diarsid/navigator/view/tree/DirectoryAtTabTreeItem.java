package diarsid.navigator.view.tree;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javafx.scene.control.TreeItem;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.model.DirectoryAtTab;
import diarsid.navigator.model.Tab;

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

        this.directory.listenForContentChanges(this::fillItemOrSetPlaceholder);

        this.setPlaceholderIfChildrenPresent();

        super.addEventHandler(TreeItem.branchExpandedEvent(), (TreeItem.TreeModificationEvent<String> event) -> {
            this.fillItem();
            this.onTreeItemExpanded.accept(this);
            event.consume();
        });

        super.addEventHandler(TreeItem.branchCollapsedEvent(), (TreeItem.TreeModificationEvent<String> event) -> {
            super.getChildren().clear();
            super.getChildren().add(PLACEHOLDER);
            this.onTreeItemCollapsed.accept(this);
            event.consume();
        });
    }

    private void fillItemOrSetPlaceholder() {
        if ( super.expandedProperty().get() ) {
            this.fillItem();
        }
        else {
            this.setPlaceholderIfChildrenPresent();
        }
    }

    private void fillItem() {
        this.directory.feedDirectories((directories) -> {
            List<TreeItem<String>> directoryItems = directories
                    .stream()
                    .filter(Directory::isNotHidden)
                    .map(directory -> this.directoryToTreeItem.apply(this.tab, directory))
                    .peek(treeItem -> System.out.println("[FILL ITEM] " + this.directory.path() + " : " + treeItem.directory.path()))
                    .sorted()
                    .peek(DirectoryAtTabTreeItem::fillItemOrSetPlaceholder)
                    .collect(toList());

            if ( directoryItems.isEmpty() ) {
                super.getChildren().clear();
                if ( super.isExpanded() ) {
                    super.setExpanded(false);
                }
            }
            else {
                super.getChildren().setAll(directoryItems);
            }
        });
    }

    public Tab tab() {
        return this.tab;
    }

    public Directory directory() {
        return this.directory;
    }

    public DirectoryAtTab directoryAtTab() {
        return this.directoryAtTab;
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

    boolean isParentOf(DirectoryAtTabTreeItem other) {
        return this.directory.isParentOf(other.directory);
    }

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
