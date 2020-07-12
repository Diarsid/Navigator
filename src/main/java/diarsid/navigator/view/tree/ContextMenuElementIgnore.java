package diarsid.navigator.view.tree;

import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

import diarsid.navigator.filesystem.FSEntry;

import static java.util.Objects.isNull;

public class ContextMenuElementIgnore extends MenuItem {

    private final TreeCell<String> treeCell;
    private final Consumer<FSEntry> doIgnore;

    public ContextMenuElementIgnore(TreeCell<String> treeCell, Consumer<FSEntry> doIgnore) {
        super("ignore");
        this.treeCell = treeCell;
        this.doIgnore = doIgnore;
        this.setOnAction(this::doOnAction);
    }

    private void doOnAction(ActionEvent actionEvent) {
        TreeItem<String> item = this.treeCell.getTreeItem();

        if ( isNull(item) ) {
            return;
        }

        if ( item instanceof DirectoriesTreeItem) {
            DirectoriesTreeItem directoryAtTabItem = (DirectoriesTreeItem) item;
            this.doIgnore.accept(directoryAtTabItem.directory());
        }
    }
}
