package diarsid.navigator.view.breadcrumbs;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import diarsid.filesystem.api.Directory;
import diarsid.filesystem.api.FileSystem;
import diarsid.navigator.model.Tab;
import diarsid.navigator.model.Tabs;
import diarsid.navigator.view.icons.Icon;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.objects.references.Listening;
import diarsid.support.objects.references.Possible;
import diarsid.support.objects.references.Result;

import static java.util.Objects.nonNull;

import static diarsid.support.objects.references.References.simplePossibleButEmpty;


public class PathBreadcrumbsBar {

    private final Tabs tabs;
    private final FileSystem fileSystem;
    private final Icons icons;
    private final BreadcrumbsBar<Directory> bar;
    private final Possible<Directory> directory;
    private final Possible<Listening<Directory>> directoryListening;
    private final Consumer<Directory> onNewDirectoryInput;

    public PathBreadcrumbsBar(
            Tabs tabs,
            FileSystem fileSystem,
            Icons icons,
            BiConsumer<Directory, MouseEvent> onDirectoryClicked,
            Consumer<Directory> onNewDirectoryInput) {
        this.tabs = tabs;
        this.fileSystem = fileSystem;
        this.icons = icons;

        this.directory = simplePossibleButEmpty();
        this.directoryListening = simplePossibleButEmpty();

        this.onNewDirectoryInput = onNewDirectoryInput;

        Function<List<Directory>, String> directoriesListToPath = (directories) -> {
            return directories.get(directories.size() - 1).path().toString();
        };

        Predicate<String> validatorAndConsumer = (path) -> {
            Result<Directory> directory = fileSystem.toDirectory(path);
            if ( directory.isEmpty() ) {
                return false;
            }
            this.onNewDirectoryInput.accept(directory.get());
            return true;
        };

        this.bar = new BreadcrumbsBar<>(">", Directory::name, directoriesListToPath, onDirectoryClicked, validatorAndConsumer);
        this.bar.size().bind(this.icons.iconSize());

        this.tabs.listenForSelectedTabChange(this::onTabsChange);
        if ( this.tabs.hasSelected() ) {
            this.acceptNewTab(this.tabs.selectedTabOrThrow());
        }
    }

    private void onTabsChange(Tab oldTab, Tab newTab) {
        acceptNewTab(newTab);
    }

    private void acceptNewTab(Tab newTab) {
        Listening<Directory> newDirectoryListening = newTab.selectedDirectory().listen(this::onDirectoryChange);
        Listening<Directory> oldDirectoryListening = this.directoryListening.resetTo(newDirectoryListening);
        if ( nonNull(oldDirectoryListening) ) {
            oldDirectoryListening.cancel();
        }
        this.directory.resetTo(newTab.selectedDirectory());
        this.directory.ifPresent(this::setPathToBar);
    }

    private void onDirectoryChange(Directory oldDirectory, Directory newDirectory) {
        if ( nonNull(newDirectory) ) {
            this.setPathToBar(newDirectory);
        }
    }

    private void setPathToBar(Directory directory) {
        this.bar.clear();

        Directory machineDirectory = this.fileSystem.machineDirectory();
        if ( ! directory.equals(machineDirectory) ) {
            this.addToBar(machineDirectory);
        }

        directory.parents().forEach(this::addToBar);
        this.addToBar(directory);
    }

    private void addToBar(Directory directory) {
        Icon icon = this.icons.getFor(directory);
        this.bar.add(icon.image(), directory);
    }

    public Node node() {
        return this.bar.node();
    }
}
