package diarsid.navigator.view.tabs;

import java.util.List;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

import diarsid.filesystem.api.Directory;
import diarsid.navigator.model.Tab;
import diarsid.navigator.view.icons.Icons;

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
    private final ImageView iconView;
    private final LabelsAtTabs labelsAtTabs;
    private final Icons icons;

    LabelAtTab(Tab tab, LabelsAtTabs labelsAtTabs, Icons icons) {
        this.tab = tab;
        this.label = new Label();
        this.labelsAtTabs = labelsAtTabs;
        this.icons = icons;

        this.iconView = new ImageView();

        ReadOnlyDoubleProperty size = this.icons.iconSize();
        this.iconView.fitWidthProperty().bind(size);
        this.iconView.fitHeightProperty().bind(size);
        this.iconView.setPreserveRatio(true);
        this.iconView.getStyleClass().add("icon");

        if ( tab.selectedDirectory().isPresent() ) {
            this.iconView.setImage(this.icons.getFor(tab.selectedDirectory().orThrow()).image());
        }
        else {
            this.iconView.setImage(this.icons.getDefaultImageForDirectory());
        }

        this.label.setPrefWidth(POSITIVE_INFINITY);
        this.label.setText(tab.name());
        this.label.setGraphic(this.iconView);
        this.label.minHeightProperty().bind(this.icons.iconSize().add(10));
        this.label.maxHeightProperty().bind(this.icons.iconSize().add(10));

        this.label.getStyleClass().add(FX_CSS_CLASS);

        tab.listenToVisibleNameChange(this::onTabVisibleNameChanged);
        tab.active().listen(this::onTabActivityChanged);
        tab.selectedDirectory().listen(this::onTabSelectedDirectoryChanged);
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

    private void onTabVisibleNameChanged(String oldName, String newName) {
        this.label.setText(newName);
    }

    private void onTabSelectedDirectoryChanged(Directory oldDirectory, Directory newDirectory) {
        this.iconView.setImage(this.icons.getFor(newDirectory).image());
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
