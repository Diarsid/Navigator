package diarsid.navigator.model;

import java.util.Objects;
import java.util.function.BiConsumer;

import diarsid.navigator.filesystem.Directory;
import diarsid.support.objects.references.Listening;
import diarsid.support.objects.references.impl.PossibleListenable;
import diarsid.support.objects.references.impl.Present;
import diarsid.support.objects.references.impl.PresentListenable;

import static java.lang.String.format;

import static diarsid.support.objects.references.impl.References.listenablePresentOf;
import static diarsid.support.objects.references.impl.References.listenable;
import static diarsid.support.objects.references.impl.References.possibleButEmpty;
import static diarsid.support.objects.references.impl.References.presentOf;


public class Tab {

    public static final String DEFAULT_NAME = "/";

    private final Identity<Tab> identity;
    private final PresentListenable<Boolean> isActive;
    private final PossibleListenable<Directory> selectedDirectory;
    private final PresentListenable<String> visibleName;
    private final Present<Boolean> isPinned;
    private final Listening<Directory> listenSelectedDirectoryToChangeVisibleName;

    Tab(Identity<Tab> identity) {
        this.identity = identity;
        this.isActive = listenablePresentOf(false, format("Tab[%s].isActive", this.identity.serial()));
        this.selectedDirectory = listenable(possibleButEmpty());
        this.visibleName = listenablePresentOf(DEFAULT_NAME, format("Tab[%s].visibleName", this.identity.serial()));
        this.isPinned = presentOf(false, format("Tab[%s].isPinned", this.identity.serial()));

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

    public PresentListenable<Boolean> active() {
        return this.isActive;
    }

    public PossibleListenable<Directory> selectedDirectory() {
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
