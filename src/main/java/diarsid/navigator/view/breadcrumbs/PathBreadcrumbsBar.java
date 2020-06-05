package diarsid.navigator.view.breadcrumbs;

import java.util.function.BiConsumer;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FileSystem;
import diarsid.navigator.model.DirectoriesAtTabs;
import diarsid.navigator.model.DirectoryAtTab;
import diarsid.navigator.model.Tab;
import diarsid.navigator.model.Tabs;
import diarsid.navigator.view.icons.Icon;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.objects.references.Reference;

public class PathBreadcrumbsBar {

    private final Tabs tabs;
    private final FileSystem fileSystem;
    private final Icons icons;
    private final DirectoriesAtTabs directoriesAtTabs;
    private final BreadcrumbsBar<DirectoryAtTab> bar;

    public PathBreadcrumbsBar(
            Tabs tabs,
            FileSystem fileSystem,
            Icons icons,
            DirectoriesAtTabs directoriesAtTabs,
            BiConsumer<DirectoryAtTab, MouseEvent> onDirectoryClicked) {
        this.tabs = tabs;
        this.fileSystem = fileSystem;
        this.icons = icons;
        this.directoriesAtTabs = directoriesAtTabs;
        this.bar = new BreadcrumbsBar<>(">", DirectoryAtTab::directoryName, onDirectoryClicked);

        this.tabs.selectedDirectory().listen(this::accept);
        this.tabs.selectedDirectory().ifPresent(this::setPathToBar);
    }

    private void accept(DirectoryAtTab oldDirectory, DirectoryAtTab newDirectory) {
        this.setPathToBar(newDirectory);
    }

    private void setPathToBar(DirectoryAtTab directoryAtTab) {
        this.bar.clear();

        Tab tab = directoryAtTab.tab();
        Directory directory = directoryAtTab.directory();

        Directory machineDirectory = this.fileSystem.machineDirectory();
        DirectoryAtTab machineDirectoryAtTab = this.directoriesAtTabs.join(tab, machineDirectory);
        if ( ! directory.equals(machineDirectory) ) {
            this.bar.add(this.icons.getFor(machineDirectory).image(), machineDirectoryAtTab);
        }

        directory.parents().forEach(parent -> {
            DirectoryAtTab parentAtTab = this.directoriesAtTabs.join(tab, parent);
            Icon icon = icons.getFor(parent);
            this.bar.add(icon.image(), parentAtTab);
        });
        Icon icon = icons.getFor(directory);
        this.bar.add(icon.image(), directoryAtTab);
    }

    public Reference<Double> iconsSize() {
        return this.bar.size();
    }

    public Node node() {
        return this.bar.node();
    }
}
