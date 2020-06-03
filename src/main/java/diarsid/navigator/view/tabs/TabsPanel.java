package diarsid.navigator.view.tabs;

import java.util.HashMap;
import java.util.List;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.model.DirectoriesAtTabs;
import diarsid.navigator.model.Tab;
import diarsid.navigator.model.Tabs;
import diarsid.navigator.view.ViewComponent;
import diarsid.navigator.view.dragdrop.DragAndDropNodes;
import diarsid.navigator.view.tree.DirectoriesTree;

import static java.lang.Double.POSITIVE_INFINITY;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.TOP_CENTER;
import static javafx.scene.input.TransferMode.MOVE;

public class TabsPanel implements ViewComponent {

    private final Tabs tabs;
    private final DirectoriesAtTabs directoriesAtTabs;
    private final VBox tabsPanel;
    private final Label addLabel;
    private final LabelsAtTabs labelsAtTabs;
    private final DirectoriesTree directoriesTree;
    private final DragAndDropNodes<Label> dragAndDropLabels;
    private final TabNames tabNames;

    public TabsPanel(
            Tabs tabs,
            DirectoriesAtTabs directoriesAtTabs,
            LabelsAtTabs labelsAtTabs,
            DirectoriesTree directoriesTree,
            DragAndDropNodes<Label> dragAndDropLabels) {
        this.tabs = tabs;
        this.directoriesAtTabs = directoriesAtTabs;
        this.labelsAtTabs = labelsAtTabs;
        this.directoriesTree = directoriesTree;
        this.labelsAtTabs.onTabsReordered(this::refresh);
        this.dragAndDropLabels = dragAndDropLabels;
        this.tabNames = new TabNames(new HashMap<>());

        this.tabsPanel = new VBox();
        this.tabsPanel.setFillWidth(true);
        this.tabsPanel.setMinWidth(10);
        this.tabsPanel.setPrefWidth(50);
        this.tabsPanel.setMaxWidth(200);

        this.tabsPanel.setOnDragOver((dragEvent) -> {
            if ( this.dragAndDropLabels.isDragOverAcceptable(dragEvent) ) {
                dragEvent.acceptTransferModes(MOVE);
                dragEvent.consume();
            }
        });

        this.tabsPanel.setOnDragDropped(dragEvent -> {
            boolean success;
            if ( this.dragAndDropLabels.isDropAcceptable(dragEvent) ) {
                try {
                    Label source = (Label) dragEvent.getGestureSource();
                    boolean isOk = this.labelsAtTabs.acceptDroppedOnPanel(source);
                    if ( isOk ) {
                        this.refresh();
                    }
                    success = isOk;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    success = false;
                }
            }
            else {
                success = false;
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });

        this.tabsPanel.setAlignment(TOP_CENTER);

        this.addLabel = new Label();
        this.addLabel.getStyleClass().add("add-tab-button");
        this.addLabel.setText("+");
        this.addLabel.setMinWidth(50);
        this.addLabel.setPrefWidth(POSITIVE_INFINITY);
        this.addLabel.setAlignment(CENTER);
        this.addLabel.setOnMouseClicked(event -> {
            this.newTab(true);
        });

        this.newTab(true);
    }

    @Override
    public Node node() {
        return tabsPanel;
    }

    public void newTab(boolean selectCreated, Directory directory) {
        Tab tab = this.tabs.createTab();

        this.directoriesTree.add(tab, directory, selectCreated);

        if ( selectCreated ) {
            this.labelsAtTabs.addAndSelect(tab);
        }
        else {
            this.labelsAtTabs.add(tab);
        }

        this.tabNames.add(tab);

        this.refresh();
    }

    public void newTab(boolean selectCreated) {
        Tab tab = this.tabs.createTab();

        this.directoriesTree.add(tab, selectCreated);

        if ( selectCreated ) {
            this.labelsAtTabs.addAndSelect(tab);
        }
        else {
            this.labelsAtTabs.add(tab);
        }

        this.tabNames.add(tab);

        this.refresh();
    }

    private void refresh() {
        this.tabsPanel.getChildren().clear();
        this.tabsPanel.getChildren().addAll(this.labelsAtTabs.tabLabels());
        this.tabsPanel.getChildren().add(this.addLabel);
    }
}
