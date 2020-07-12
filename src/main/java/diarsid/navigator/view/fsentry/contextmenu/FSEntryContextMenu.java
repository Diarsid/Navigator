package diarsid.navigator.view.fsentry.contextmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.control.ContextMenu;
import javafx.stage.WindowEvent;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.filesystem.FileSystem;
import diarsid.support.objects.references.impl.Possible;
import diarsid.support.objects.references.impl.PossibleListenable;

import static diarsid.support.objects.references.impl.References.listenable;
import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class FSEntryContextMenu extends ContextMenu {

    private final Supplier<FSEntry> fsEntrySource;
    private final PossibleListenable<FSEntry> fsEntry;
    private final List<FSEntryMenuItem> items;

    FSEntryContextMenu(Supplier<FSEntry> fsEntrySource, FileSystem fileSystem, Consumer<FSEntry> onIgnore) {
        this.fsEntry = listenable(possibleButEmpty());
        this.fsEntrySource = fsEntrySource;
        this.items = new ArrayList<>();

        super.setOnHiding(this::doOnHiding);
        super.setOnShowing(this::doOnShowing);

        FSEntryMenuItem show = new FSEntryMenuItemShowInDefaultManager(this.fsEntry);
        FSEntryMenuItem remove = new FSEntryMenuItemRemove(this.fsEntry, fileSystem);
        FSEntryMenuItem ignore = new FSEntryMenuItemIgnore(this.fsEntry, onIgnore);

        this.items.add(show);
        this.items.add(remove);
        this.items.add(ignore);

        super.getItems().setAll(this.items);
    }

    private void doOnShowing(WindowEvent windowEvent) {
        this.fsEntry.resetTo(this.fsEntrySource);

        if ( this.fsEntry.isNotPresent() ) {
            windowEvent.consume();
        }
    }

    public Possible<FSEntry> fsEntry() {
        return this.fsEntry;
    }

    private void doOnHiding(WindowEvent windowEvent) {

    }

}
