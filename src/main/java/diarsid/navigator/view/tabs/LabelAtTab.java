package diarsid.navigator.view.tabs;

import java.util.List;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

import diarsid.navigator.model.Tab;

import static java.lang.Double.POSITIVE_INFINITY;
import static javafx.scene.input.TransferMode.MOVE;

public class LabelAtTab {

    private final static String FX_CSS_CLASS;
    private final static PseudoClass SELECTED;
    private final static PseudoClass MOVED;
    private final static PseudoClass REPLACE_CANDIDATE;

    static {
        FX_CSS_CLASS = "label-at-tab";
        SELECTED = PseudoClass.getPseudoClass("selected");
        MOVED = PseudoClass.getPseudoClass("moved");
        REPLACE_CANDIDATE = PseudoClass.getPseudoClass("replace-candidate");
    }

    private final Tab tab;
    private final Label label;
    private final LabelsAtTabs labelsAtTabs;

    LabelAtTab(Tab tab, LabelsAtTabs labelsAtTabs) {
        this.tab = tab;
        this.label = new Label();
        this.labelsAtTabs = labelsAtTabs;

        this.label.setPrefWidth(POSITIVE_INFINITY);
        this.label.setText(tab.name());

        this.label.getStyleClass().add(FX_CSS_CLASS);

        tab.listenToRename(this::onTabNameChanged);
        tab.active().listen(this::onTabActivityChanged);
        this.onTabActivityChanged(null, tab.active().get());

        this.label.setOnMouseClicked(this::onMouseClicked);
        this.label.setOnDragOver(this::onDragOver);
        this.label.setOnDragExited(this::onDragExited);
        this.label.setOnDragDropped(this::onDragDropped);
        this.label.setOnDragDetected(this::onDragDetected);
    }

    public Tab tab() {
        return this.tab;
    }

    public Label label() {
        return this.label;
    }

    private void onTabNameChanged(String oldName, String newName) {
        this.label.setText(newName);
    }

    private void onTabActivityChanged(Boolean oldActivity, Boolean newActivity) {
        if ( newActivity ) {
            this.label.pseudoClassStateChanged(SELECTED, true);
        }
        else {
            this.label.pseudoClassStateChanged(SELECTED, false);
        }
    }

    private void onDragDetected(MouseEvent event) {
        this.labelsAtTabs.dragAndDropLabels.setTabDraggedContextTo(this.label);
        this.label.pseudoClassStateChanged(MOVED, true);
        event.consume();
    };

    private void onDragOver(DragEvent dragEvent) {
        if ( this.labelsAtTabs.dragAndDropLabels.isDragOverAcceptable(dragEvent) ) {
            dragEvent.acceptTransferModes(MOVE);
            dragEvent.consume();
            this.label.pseudoClassStateChanged(REPLACE_CANDIDATE, true);
        }
        if ( this.labelsAtTabs.dragAndDropFiles.isDragAcceptable(dragEvent) ) {
            dragEvent.acceptTransferModes(MOVE);
            dragEvent.consume();
        }
    };

    private void onDragExited(DragEvent dragEvent) {
        if ( this.labelsAtTabs.dragAndDropLabels.isDragOverAcceptable(dragEvent) ) {
            this.label.pseudoClassStateChanged(REPLACE_CANDIDATE, false);
        }
    };

    private void onDragDropped(DragEvent dragEvent) {
        boolean success;

        boolean isDropAcceptable = this.labelsAtTabs.dragAndDropLabels.isDropAcceptable(dragEvent);
        if ( isDropAcceptable && this.labelsAtTabs.onTabsReorderedAction.isPresent() ) {
            try {
                Label draggedTab = (Label) dragEvent.getGestureSource();
                List<Label> draggableTabLabels = this.labelsAtTabs.tabLabels();
                int tabInsertIndex = draggableTabLabels.indexOf(this.label);

                draggedTab.pseudoClassStateChanged(MOVED, false);
                this.label.pseudoClassStateChanged(REPLACE_CANDIDATE, false);

                draggableTabLabels.remove(draggedTab);
                draggableTabLabels.add(tabInsertIndex, draggedTab);

                this.labelsAtTabs.onTabsReorderedAction.orThrow().run();

                success = true;
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
    }

    private void onMouseClicked(MouseEvent event) {
        this.labelsAtTabs.onTabTabLabelClicked.accept(this.tab);
    }
}
