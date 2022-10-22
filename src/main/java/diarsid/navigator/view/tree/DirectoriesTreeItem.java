package diarsid.navigator.view.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

import diarsid.filesystem.api.Directory;
import diarsid.navigator.model.Tab;
import diarsid.support.strings.MultilineMessage;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

public class DirectoriesTreeItem extends TreeItem<String> implements Comparable<DirectoriesTreeItem> {

    public static final Comparator<TreeItem<String>> COMPARATOR = (item1, item2) -> {
        if ( (item1 instanceof DirectoriesTreeItem) && (item2 instanceof DirectoriesTreeItem) ) {
            DirectoriesTreeItem directoryItem1 = (DirectoriesTreeItem) item1;
            DirectoriesTreeItem directoryItem2 = (DirectoriesTreeItem) item2;
            return directoryItem1.compareTo(directoryItem2);
        }
        else {
            return item1.getValue().compareTo(item2.getValue());
        }
    };

    private static final TreeItem<String> PLACEHOLDER = new TreeItem<>("...");

    private final Tab tab;
    private final Directory directory;
    private final Consumer<DirectoriesTreeItem> onTreeItemExpanded;
    private final Consumer<DirectoriesTreeItem> onTreeItemCollapsed;

    DirectoriesTreeItem(
            Tab tab,
            Directory directory,
            Consumer<DirectoriesTreeItem> onTreeItemExpanded,
            Consumer<DirectoriesTreeItem> onTreeItemCollapsed) {
        super(directory.name());
        this.tab = tab;
        this.directory = directory;
        this.onTreeItemExpanded = onTreeItemExpanded;
        this.onTreeItemCollapsed = onTreeItemCollapsed;

        this.setPlaceholderIfChildrenPresent();

        super.expandedProperty().addListener(this::onExpandedPropertyChange);
    }

    void fill() {
        if ( this.directory.isAbsent() ) {
            super.getChildren().clear();
            return;
        }

        this.directory.feedDirectories((directories) -> {
            List<TreeItem<String>> directoryItems = directories
                    .stream()
                    .filter(Directory::isNotHidden)
                    .map(this::makeFor)
                    .sorted()
                    .peek(DirectoriesTreeItem::setPlaceholderIfChildrenPresent)
                    .peek(DirectoriesTreeItem::watchDirectory)
                    .collect(toList());

            if ( directoryItems.isEmpty() ) {
                super.getChildren().clear();
                if ( super.isExpanded() ) {
                    super.setExpanded(false);
                }
            }
            else if ( this.isNotFilled() ) {
                super.getChildren().setAll(directoryItems);
                MultilineMessage message = new MultilineMessage("[TREE] [ITEM FILL]", "   ");
                message.newLine().add(this.directory.path().toString());
                super.getChildren().forEach((item) -> message.newLine().indent().add(item.getValue()));
                System.out.println(message.compose());
            }
            else {
                List<TreeItem<String>> itemsToAdd = new ArrayList<>();
                for ( TreeItem<String> item : directoryItems ) {
                    if ( ! this.containsNameInChildren(item.getValue()) ) {
                        itemsToAdd.add(item);
                    }
                }

                if ( ! itemsToAdd.isEmpty() ) {
                    super.getChildren().addAll(itemsToAdd);
                    super.getChildren().sort(COMPARATOR);

                    MultilineMessage message = new MultilineMessage("[TREE] [ITEM ADD]", "   ");
                    message.newLine().add(this.directory.path().toString());
                    itemsToAdd.forEach((item) -> message.newLine().indent().add(item.getValue()));
                    System.out.println(message.compose());
                }
            }
        });
    }

    private boolean containsNameInChildren(String name) {
        for ( TreeItem<String> item : super.getChildren() ) {
            if ( item.getValue().equals(name) ) {
                return true;
            }
        }

        return false;
    }

    private void watchDirectory() {
        this.directory.watch();
    }

    private DirectoriesTreeItem makeFor(Directory directory) {
        return new DirectoriesTreeItem(this.tab, directory, this.onTreeItemExpanded, this.onTreeItemCollapsed);
    }

    public Directory directory() {
        return this.directory;
    }

    public Tab tab() {
        return this.tab;
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
    public int compareTo(DirectoriesTreeItem other) {
        return this.directory.compareTo(other.directory);
    }

    boolean isParentOf(DirectoriesTreeItem other) {
        return this.directory.isIndirectParentOf(other.directory);
    }

    DirectoriesTreeItem getInChildrenOrNull(Directory someDirectory) {
        for ( TreeItem<String> child : super.getChildren() ) {
            if ( child.getValue().equalsIgnoreCase(someDirectory.name()) ) {
                return (DirectoriesTreeItem) child;
            }
        }
        return null;
    }

    DirectoriesTreeItem getInChildrenOrNull(String someDirectory) {
        for ( TreeItem<String> child : super.getChildren() ) {
            if ( child.getValue().equalsIgnoreCase(someDirectory) ) {
                return (DirectoriesTreeItem) child;
            }
        }
        return null;
    }

    DirectoriesTreeItem getInChildrenOrCreate(Directory someDirectory) {
        if ( this.isNotFilled() ) {
            this.fill();
        }

        DirectoriesTreeItem directoryItem = this.getInChildrenOrNull(someDirectory);

        if ( nonNull(directoryItem) ) {
            return directoryItem;
        }

        if ( this.directory.isIndirectParentOf(someDirectory) ) {
            directoryItem = this.makeFor(someDirectory);
            super.getChildren().add(directoryItem);
            super.getChildren().sort(COMPARATOR);
            return directoryItem;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    void expandIfNotExpanded() {
        if ( this.isExpanded() ) {
            return;
        }

        if ( this.isLeaf() ) {
            return;
        }

        this.setExpanded(true);
    }

    boolean isFilled() {
        return super.getChildren().size() > 1 || ( ! super.getChildren().contains(PLACEHOLDER) );
    }

    boolean isNotFilled() {
        return super.getChildren().size() == 1 && super.getChildren().contains(PLACEHOLDER);
    }

    boolean removeInChildren(String name) {
        if ( this.isNotFilled() ) {
            this.fill();
        }

        if ( super.getChildren().isEmpty() ) {
            return true;
        }

        TreeItem<String> childToRemove = null;

        for ( TreeItem<String> child : super.getChildren() ) {
            if ( child.getValue().equalsIgnoreCase(name) ) {
                childToRemove = child;
                break;
            }
        }

        boolean removed;
        if ( nonNull(childToRemove) ) {
            super.getChildren().remove(childToRemove);
            removed = true;
        }
        else {
            removed = false;
        }

        return removed;
    }

    boolean removeInChildren(Directory someDirectory) {
        if ( this.isNotFilled() ) {
            this.fill();
        }

        TreeItem<String> childToRemove = null;

        for ( TreeItem<String> child : super.getChildren() ) {
            if ( child.getValue().equalsIgnoreCase(someDirectory.name()) ) {
                childToRemove = child;
                break;
            }
        }

        boolean removed;
        if ( nonNull(childToRemove) ) {
            super.getChildren().remove(childToRemove);
            removed = true;
        }
        else {
            removed = false;
        }

        return removed;
    }

    private void onTreeItemExpanded() {
        this.fill();
        this.onTreeItemExpanded.accept(this);
    }

    private void onTreeItemCollapsed() {
        this.onTreeItemCollapsed.accept(this);
    }

    private void onExpandedPropertyChange(ObservableValue<? extends Boolean> property, Boolean oldValue, Boolean newValue) {
        if ( newValue == oldValue ) {
            System.out.println("expanded : old=new");
        }
        if ( newValue ) {
            this.onTreeItemExpanded();
        } else {
            this.onTreeItemCollapsed();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectoriesTreeItem)) return false;
        DirectoriesTreeItem that = (DirectoriesTreeItem) o;
        return directory.equals(that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory);
    }
}
