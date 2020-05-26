package diarsid.beam.navigator.view.tabs;

import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import diarsid.beam.navigator.filesystem.Directory;
import diarsid.beam.navigator.model.DirectoriesAtTabs;
import diarsid.beam.navigator.model.Tab;
import diarsid.beam.navigator.model.Tabs;
import diarsid.beam.navigator.view.ViewComponent;
import diarsid.beam.navigator.view.dragdrop.DragAndDropContext;
import diarsid.beam.navigator.view.tree.DirectoriesTree;

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
    private final DragAndDropContext<Label> dragAndDropContext;
    private final TabNames tabNames;

    public TabsPanel(
            Tabs tabs,
            DirectoriesAtTabs directoriesAtTabs,
            LabelsAtTabs labelsAtTabs,
            DirectoriesTree directoriesTree,
            DragAndDropContext<Label> dragAndDropContext) {
        this.tabs = tabs;
        this.directoriesAtTabs = directoriesAtTabs;
        this.labelsAtTabs = labelsAtTabs;
        this.directoriesTree = directoriesTree;
        this.labelsAtTabs.onTabsReordered(this::refresh);
        this.dragAndDropContext = dragAndDropContext;
        this.tabNames = new TabNames(new HashMap<>());

        this.tabsPanel = new VBox();
        this.tabsPanel.setFillWidth(true);
        this.tabsPanel.setMinWidth(10);
        this.tabsPanel.setPrefWidth(50);
        this.tabsPanel.setMaxWidth(200);

        this.tabsPanel.setOnDragOver((dragEvent) -> {
            if ( this.dragAndDropContext.isDragOverAcceptable(dragEvent) ) {
                dragEvent.acceptTransferModes(MOVE);
                dragEvent.consume();
            }
        });

        this.tabsPanel.setOnDragDropped(dragEvent -> {
            boolean success;
            if ( this.dragAndDropContext.isDropAcceptable(dragEvent) ) {
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
        this.addLabel.setStyle("-fx-background-color: white;");
        this.addLabel.setText("+");
        this.addLabel.setMinWidth(50);
        this.addLabel.setPrefWidth(Double.POSITIVE_INFINITY);
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
