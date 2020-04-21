package diarsid.beam.navigator.view.filetree;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import javafx.scene.control.TreeItem;

import diarsid.beam.navigator.filesystem.Directory;
import diarsid.beam.navigator.model.DirectoryAtTab;
import diarsid.beam.navigator.model.Tab;

import static java.lang.String.format;

public class DirectoryAtTabTreeItem extends TreeItem<String> {

    private final static TreeItem<String> PLACEHOLDER = new TreeItem<>("...");
    private final Tab tab;
    private final Directory directory;
    private final DirectoryAtTab directoryAtTab;
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
        this.onTreeItemExpanded = onTreeItemExpanded;
        this.onTreeItemCollapsed = onTreeItemCollapsed;

        this.directory.checkDirectoriesPresence((present) -> {
            if (present) {
                super.getChildren().add(PLACEHOLDER);
            }
        });

        super.addEventHandler(TreeItem.branchExpandedEvent(), (TreeItem.TreeModificationEvent<String> event) -> {
            System.out.println("expanded " + this.directory.name());
            super.getChildren().clear();

            this.directory.feedDirectories((directories) -> {
                directories
                        .stream()
                        .filter(Directory::isNotHidden)
                        .map(directory -> directoryToTreeItem.apply(this.tab, directory))
                        .forEach(super.getChildren()::add);
            });

            this.tab.selection().ifPresent(directoryAtTab1 -> this.onTreeItemExpanded.accept(this));

            event.consume();
        });

        super.addEventHandler(TreeItem.branchCollapsedEvent(), (TreeItem.TreeModificationEvent<String> event) -> {
            System.out.println("collapsed " + this.directory.name());
            super.getChildren().clear();
            super.getChildren().add(PLACEHOLDER);
            this.tab.selection().ifPresent(directoryAtTab1 -> this.onTreeItemCollapsed.accept(this));
            event.consume();
        });

        System.out.println(format("%s created - %s", this.getClass().getSimpleName(), directory.name()));
    }

    public void selected() {
        tab.selection().resetTo(directoryAtTab);
    }

    public Tab tab() {
        return tab;
    }

    public Directory directory() {
        return directory;
    }

    public DirectoryAtTab directoryAtTab() {
        return directoryAtTab;
    }
}
