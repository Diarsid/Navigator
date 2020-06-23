package diarsid.navigator.view.fsentry.contextmenu;

import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.tree.DirectoryAtTabTreeItem;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public class FSEntryMenuItemIgnore extends FSEntryMenuItem {

    public FSEntryMenuItemIgnore() {
        super();
        this.setOnAction(this::doOnAction);
    }

    private void doOnAction(ActionEvent actionEvent) {

    }

    @Override
    String toText(FSEntry fsEntry) {
        return format("ignore '%s' %s", fsEntry.name(), fsEntry.isDirectory() ? "directory" : "file");
    }

    @Override
    Node toGraphic(FSEntry fsEntry) {
        return null;
    }
}
