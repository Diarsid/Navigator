package diarsid.navigator.view.breadcrumbs;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
import diarsid.support.objects.groups.Running;
import diarsid.support.objects.references.Reference;
import diarsid.support.objects.references.impl.Possible;

import static diarsid.navigator.filesystem.Directory.Edit.MOVED;
import static diarsid.navigator.filesystem.Directory.Edit.RENAMED;
import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class PathBreadcrumbsBar {

    private final Tabs tabs;
    private final FileSystem fileSystem;
    private final Icons icons;
    private final DirectoriesAtTabs directoriesAtTabs;
    private final BreadcrumbsBar<DirectoryAtTab> bar;
    private final Possible<DirectoryAtTab> directory;
    private final Possible<Running> directoryListening;
    private final Consumer<DirectoryAtTab> onNewDirectoryInput;

    public PathBreadcrumbsBar(
            Tabs tabs,
            FileSystem fileSystem,
            Icons icons,
            DirectoriesAtTabs directoriesAtTabs,
            BiConsumer<DirectoryAtTab, MouseEvent> onDirectoryClicked,
            Consumer<DirectoryAtTab> onNewDirectoryInput) {
        this.tabs = tabs;
        this.fileSystem = fileSystem;
        this.icons = icons;
        this.directoriesAtTabs = directoriesAtTabs;

        this.directory = possibleButEmpty();
        this.directoryListening = possibleButEmpty();

        this.onNewDirectoryInput = onNewDirectoryInput;

        Function<List<DirectoryAtTab>, String> directoriesListToPath = (directories) -> {
            return directories.get(directories.size() - 1).directory().path().toString();
        };

        Predicate<String> validatorAndConsumer = (path) -> {
            Optional<Directory> directory = fileSystem.toDirectory(path);
            if ( directory.isEmpty() ) {
                return false;
            }
            Tab tab = this.tabs.selected().orThrow();
            DirectoryAtTab directoryAtTab = this.directoriesAtTabs.join(tab, directory.get());
            this.onNewDirectoryInput.accept(directoryAtTab);
            return true;
        };
        this.bar = new BreadcrumbsBar<>(">", DirectoryAtTab::directoryName, directoriesListToPath, onDirectoryClicked, validatorAndConsumer);

        this.tabs.selectedDirectory().listen(this::accept);
        this.tabs.selectedDirectory().ifPresent(this::setPathToBar);
    }

    private void accept(DirectoryAtTab oldDirectoryAtTab, DirectoryAtTab newDirectoryAtTab) {
        this.directory.resetTo(newDirectoryAtTab);
        this.directoryListening.ifPresent(Running::cancel);
        Directory directory = newDirectoryAtTab.directory();
        if ( directory.canBe(MOVED) || directory.canBe(RENAMED) ) {
            this.directoryListening.resetTo(directory.listenForChanges(this::onDirectoryChanged));
        }
        this.setPathToBar(newDirectoryAtTab);
    }

    private void setPathToBar(DirectoryAtTab directoryAtTab) {
        this.bar.clear();
        Tab tab = directoryAtTab.tab();
        Directory directory = directoryAtTab.directory();

        Directory machineDirectory = this.fileSystem.machineDirectory();
        DirectoryAtTab machineDirectoryAtTab = this.directoriesAtTabs.join(tab, machineDirectory);
        if ( ! directory.equals(machineDirectory) ) {
            this.addToBar(machineDirectoryAtTab);
        }

        directory
                .parents()
                .stream()
                .map(parent -> this.directoriesAtTabs.join(tab, parent))
                .forEach(this::addToBar);
        this.addToBar(directoryAtTab);
    }

    private void addToBar(DirectoryAtTab directoryAtTab) {
        Icon icon = this.icons.getFor(directoryAtTab.directory());
        this.bar.add(icon.image(), directoryAtTab);
    }

    public Reference<Double> iconsSize() {
        return this.bar.size();
    }

    public Node node() {
        return this.bar.node();
    }

    private void onDirectoryChanged() {
        this.directory.ifPresent(this::setPathToBar);
    }
}
