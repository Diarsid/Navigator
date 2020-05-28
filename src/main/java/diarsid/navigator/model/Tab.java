package diarsid.navigator.model;

import java.util.Objects;
import java.util.function.BiConsumer;

import diarsid.support.objects.references.Listening;
import diarsid.support.objects.references.real.PossibleListenable;
import diarsid.support.objects.references.real.Present;
import diarsid.support.objects.references.real.PresentListenable;

import static java.lang.String.format;

import static diarsid.support.objects.references.real.Possibles.listeneable;
import static diarsid.support.objects.references.real.Possibles.possibleButEmpty;
import static diarsid.support.objects.references.real.Presents.listenablePresent;
import static diarsid.support.objects.references.real.Presents.presentOf;

public class Tab {

    public static final String DEFAULT_NAME = "/";

    private final Identity<Tab> identity;
    private final PresentListenable<Boolean> isActive;
    private final PossibleListenable<DirectoryAtTab> selectedDirectory;
    private final PresentListenable<String> visibleName;
    private final Present<Boolean> isPinned;
    private final Listening<DirectoryAtTab> listenSelectedDirectoryToChangeVisibleName;

    Tab(Identity<Tab> identity) {
        this.identity = identity;
        this.isActive = listenablePresent(false, format("Tab[%s].isActive", this.identity.serial()));
        this.selectedDirectory = listeneable(possibleButEmpty());
        this.visibleName = listenablePresent(DEFAULT_NAME, format("Tab[%s].visibleName", this.identity.serial()));
        this.isPinned = presentOf(false, format("Tab[%s].isPinned", this.identity.serial()));

        this.listenSelectedDirectoryToChangeVisibleName = this.selectedDirectory.listen((oldSelected, newSelected) -> {
            this.visibleName.resetTo(newSelected.directory().name());
        });
    }

    public Listening<String> listenToRename(BiConsumer<String, String> listener) {
        return this.visibleName.listen(listener);
    }

    public PresentListenable<Boolean> active() {
        return this.isActive;
    }

    public PossibleListenable<DirectoryAtTab> selectedDirectory() {
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
