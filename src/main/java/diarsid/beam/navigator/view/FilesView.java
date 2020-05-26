package diarsid.beam.navigator.view;

import javafx.scene.Node;
import javafx.scene.control.SplitPane;

import diarsid.beam.navigator.view.tree.DirectoriesTree;
import diarsid.beam.navigator.view.table.FilesTable;
import diarsid.beam.navigator.view.tabs.TabsPanel;

public class FilesView implements ViewComponent {

    private final SplitPane splitPane;

    public FilesView(TabsPanel tabsPanel, DirectoriesTree directoriesTree, FilesTable filesTable) {
        this.splitPane = new SplitPane();

        this.splitPane.getItems().addAll(tabsPanel.node(), directoriesTree.node(), filesTable.node());
        this.splitPane.setDividerPositions(0.1, 0.4, 0.9);
        SplitPane.setResizableWithParent(tabsPanel.node(), false);
        SplitPane.setResizableWithParent(directoriesTree.node(), false);
    }

    @Override
    public Node node() {
        return this.splitPane;
    }

}
