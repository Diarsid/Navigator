package diarsid.navigator.view.fsentry.contextmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javafx.scene.control.ContextMenu;
import javafx.stage.WindowEvent;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.support.objects.references.impl.Possible;
import diarsid.support.objects.references.impl.PossibleListenable;

import static diarsid.support.objects.references.impl.References.listenable;
import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class FSEntryContextMenu extends ContextMenu {

    private final Supplier<FSEntry> fsEntrySource;
    private final PossibleListenable<FSEntry> fsEntry;
    private final List<FSEntryMenuItem> items;

    public FSEntryContextMenu(Supplier<FSEntry> fsEntrySource) {
        this.fsEntry = listenable(possibleButEmpty());
        this.fsEntrySource = fsEntrySource;
        this.items = new ArrayList<>();

        super.setOnHiding(this::doOnHiding);
        super.setOnShowing(this::doOnShowing);

        FSEntryMenuItem ignore = new FSEntryMenuItemIgnore();
        fsEntry.listen(ignore);
        items.add(ignore);

        super.getItems().setAll(this.items);
    }

    private void doOnShowing(WindowEvent windowEvent) {
        this.fsEntry.resetTo(this.fsEntrySource);
//        super.getItems().setAll(this.items);
    }

    public Possible<FSEntry> fsEntry() {
        return this.fsEntry;
    }

    private void doOnHiding(WindowEvent windowEvent) {

//        super.getItems().clear();
    }

}
