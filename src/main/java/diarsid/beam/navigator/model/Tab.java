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

    private final UUID uuid;
    private final PossibleListenable<DirectoryAtTab> selection;
    private final PresentListenable<String> visibleName;
    private final Present<Boolean> isPinned;

    public Tab(String name) {
        this.uuid = randomUUID();
        this.selection = listeneable(possibleButEmpty());
        this.visibleName = listenablePresent("/", format("Tab[%s].visibleName", this.uuid));
        this.isPinned = presentOf(false, format("Tab[%s].isPinned", this.uuid));

        this.selection.listen((oldSelected, newSelected) -> {
            this.visibleName.resetTo(newSelected.directory().name());
        });
    }

    public Listening<String> listenToRename(BiConsumer<String, String> listener) {
        return this.visibleName.listen(listener);
    }

    public PossibleListenable<DirectoryAtTab> selection() {
        return this.selection;
    }

    public String name() {
        return this.visibleName.get();
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
