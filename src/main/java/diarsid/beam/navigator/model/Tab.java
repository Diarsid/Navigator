package diarsid.beam.navigator.model;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

import diarsid.support.objects.references.Listening;
import diarsid.support.objects.references.real.PossibleListenable;
import diarsid.support.objects.references.real.Present;
import diarsid.support.objects.references.real.PresentListenable;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;

import static diarsid.support.objects.references.real.Possibles.listeneable;
import static diarsid.support.objects.references.real.Possibles.possibleButEmpty;
import static diarsid.support.objects.references.real.Presents.listenablePresent;
import static diarsid.support.objects.references.real.Presents.presentOf;

public class Tab {

    public static final String DEFAULT_NAME = "/";

    private final UUID uuid;
    private final PresentListenable<Boolean> isActive;
    private final PossibleListenable<DirectoryAtTab> selectedDirectory;
    private final PresentListenable<String> visibleName;
    private final Present<Boolean> isPinned;
    private final Listening<DirectoryAtTab> listenSelectedDirectoryToChangeVisibleName;

    Tab() {
        this.uuid = randomUUID();
        this.isActive = listenablePresent(false, format("Tab[%s].isActive", this.uuid));
        this.selectedDirectory = listeneable(possibleButEmpty());
        this.visibleName = listenablePresent(DEFAULT_NAME, format("Tab[%s].visibleName", this.uuid));
        this.isPinned = presentOf(false, format("Tab[%s].isPinned", this.uuid));

        this.listenSelectedDirectoryToChangeVisibleName = this.selectedDirectory.listen((oldSelected, newSelected) -> {
            this.visibleName.resetTo(newSelected.directory().name());
        });
        System.out.println("New Tab");
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
        return uuid.equals(tab.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
