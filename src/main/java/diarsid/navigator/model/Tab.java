package diarsid.navigator.model;

import java.util.Objects;
import java.util.function.BiConsumer;

import diarsid.filesystem.api.Directory;
import diarsid.support.objects.references.Listening;
import diarsid.support.objects.references.PossibleProperty;
import diarsid.support.objects.references.PresentProperty;

import static java.lang.String.format;

import static diarsid.support.objects.references.References.possiblePropertyButEmpty;
import static diarsid.support.objects.references.References.presentPropertyOf;


public class Tab {

    public static final String DEFAULT_NAME = "/";

    private final Identity<Tab> identity;
    private final PresentProperty<Boolean> isActive;
    private final PossibleProperty<Directory> selectedDirectory;
    private final PresentProperty<String> visibleName;
    private final PresentProperty<Boolean> isPinned;
    private final Listening<Directory> listenSelectedDirectoryToChangeVisibleName;

    Tab(Identity<Tab> identity) {
        this.identity = identity;
        this.isActive = presentPropertyOf(false, format("Tab[%s].isActive", this.identity.serial()));
        this.selectedDirectory = possiblePropertyButEmpty();
        this.visibleName = presentPropertyOf(DEFAULT_NAME, format("Tab[%s].visibleName", this.identity.serial()));
        this.isPinned = presentPropertyOf(false, format("Tab[%s].isPinned", this.identity.serial()));

        this.listenSelectedDirectoryToChangeVisibleName = this.selectedDirectory.listen((oldSelected, newSelected) -> {
            this.visibleName.resetTo(newSelected.name());
        });
    }

    public Identity<Tab> identity() {
        return this.identity;
    }

    public Listening<String> listenToRename(BiConsumer<String, String> listener) {
        return this.visibleName.listen(listener);
    }

    public PresentProperty<Boolean> active() {
        return this.isActive;
    }

    public PossibleProperty<Directory> selectedDirectory() {
        return this.selectedDirectory;
    }

    public String name() {
        return this.visibleName.get();
    }

    public void appendPathToVisibleName(String path) {
        this.visibleName.modify(oldName -> path + "/" + oldName);
    }

    public boolean hasSelection() {
        return this.selectedDirectory.isPresent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tab)) return false;
        Tab tab = (Tab) o;
        return this.identity.uuid().equals(tab.identity.uuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identity.uuid());
    }

    @Override
    public String toString() {
        return "Tab{" +
                "identity=" + identity +
                '}';
    }
}
