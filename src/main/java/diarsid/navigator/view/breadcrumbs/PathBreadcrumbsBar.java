package diarsid.navigator.view.breadcrumbs;

import javafx.scene.Node;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FS;
import diarsid.navigator.model.DirectoryAtTab;
import diarsid.navigator.model.Tabs;
import diarsid.navigator.view.icons.Icon;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.objects.references.Reference;

public class PathBreadcrumbsBar {

    private final Tabs tabs;
    private final FS fs;
    private final Icons icons;
    private final BreadcrumbsBar<Directory> bar;

    public PathBreadcrumbsBar(Tabs tabs, FS fs, Icons icons) {
        this.tabs = tabs;
        this.fs = fs;
        this.icons = icons;
        this.bar = new BreadcrumbsBar<>(">", Directory::name);

        this.tabs.selectedDirectory().listen(this::accept);
        this.tabs.selectedDirectory().ifPresent(this::setPathToBar);
    }

    private void accept(DirectoryAtTab oldDirectory, DirectoryAtTab newDirectory) {
        this.setPathToBar(newDirectory);
    }

    private void setPathToBar(DirectoryAtTab directoryAtTab) {
        this.bar.clear();

        Directory directory = directoryAtTab.directory();

        Directory machineDirectory = this.fs.machineDirectory();
        if ( ! directory.equals(machineDirectory) ) {
            this.bar.add(this.icons.getFor(machineDirectory).image(), machineDirectory);
        }

        directory.parents().forEach(parent -> {
            Icon icon = icons.getFor(parent);
            this.bar.add(icon.image(), parent);
        });
        Icon icon = icons.getFor(directory);
        this.bar.add(icon.image(), directory);
    }

    public Reference<Double> size() {
        return this.bar.size();
    }

    public Node node() {
        return this.bar.node();
    }
}
