package diarsid.navigator.view;

import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;

import diarsid.navigator.breadcrumbs.BreadcrumbsBar;
import diarsid.navigator.breadcrumbs.PathBreadcrumbsBar;
import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FS;
import diarsid.navigator.view.icons.Icons;
import diarsid.navigator.view.table.FilesTable;
import diarsid.navigator.view.tabs.TabsPanel;
import diarsid.navigator.view.tree.DirectoriesTree;

import static javafx.scene.layout.Priority.ALWAYS;

public class FilesView implements ViewComponent {

    private final SplitPane splitPane;

    public FilesView(
            TabsPanel tabsPanel,
            DirectoriesTree directoriesTree,
            FilesTable filesTable,
            PathBreadcrumbsBar pathBreadcrumbsBar) {
        this.splitPane = new SplitPane();

        VBox vBox = new VBox();

        SplitPane splitPane2 = new SplitPane();
        splitPane2.getStyleClass().add("files-view");
        splitPane2.getItems().addAll(directoriesTree.node(), filesTable.node());
        splitPane2.setDividerPositions(0.3, 0.7);
        VBox.setVgrow(splitPane2, ALWAYS);

        vBox.getChildren().addAll(pathBreadcrumbsBar.node(), splitPane2);
        vBox.autosize();


        this.splitPane.getItems().addAll(tabsPanel.node(), vBox);
        this.splitPane.setDividerPositions(0.1, 0.9);
        SplitPane.setResizableWithParent(tabsPanel.node(), false);
        SplitPane.setResizableWithParent(directoriesTree.node(), false);
    }

    @Override
    public Node node() {
        return this.splitPane;
    }

}
