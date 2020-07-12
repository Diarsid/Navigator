package diarsid.navigator.view.tree;

import javafx.scene.control.TreeItem;

public class DirectoriesTreeTabRoot extends TreeItem<String> {

    public DirectoriesTreeTabRoot(DirectoriesTreeItem machineDirectoryTreeItem, TreeItem<String> network) {
        super();
        this.setExpanded(true);
        this.getChildren().add(machineDirectoryTreeItem);
        this.getChildren().add(network);
    }
}
