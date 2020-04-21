package diarsid.beam.navigator.view.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import diarsid.beam.navigator.model.Tab;
import diarsid.beam.navigator.view.filetree.DirectoryAtTabTreeItem;

public class LabelsAtTabs {

    private static final String TAB_DRAG_KEY = "Label";

    private final AtomicReference<Label> draggingTab;
    private final List<Label> draggableTabLabels;
    private final VBox tabsPanel;
    private final Label add;

    private final BiConsumer<LabelAtTab, MouseEvent> onDragDetected;
    private final BiConsumer<LabelAtTab, DragEvent> onDragOver;
    private final BiConsumer<LabelAtTab, DragEvent> onDragDropped;

    public LabelsAtTabs() {
        this.draggableTabLabels = new ArrayList<>();
        this.draggingTab = new AtomicReference<>();

        this.tabsPanel = new VBox();
        this.tabsPanel.setFillWidth(true);
        this.tabsPanel.setMinWidth(10);
        this.tabsPanel.setPrefWidth(50);
        this.tabsPanel.setMaxWidth(200);

        this.tabsPanel.setOnDragOver((event) -> {
            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()
                    && TAB_DRAG_KEY.equals(dragboard.getString())
                    && draggingTab.get() != null) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        this.tabsPanel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                Label source = (Label) event.getGestureSource();
                this.tabsPanel.getChildren().add(source);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        this.add = new Label();
        this.add.setStyle("-fx-background-color: lightgrey;");
        this.add.setText("+");
        this.add.setMinWidth(50);

        this.tabsPanel.getChildren().add(this.add);

        this.onDragDetected = (labelAtTab, event) -> {
            Label label = labelAtTab.label();
            Dragboard dragboard = label.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(TAB_DRAG_KEY);
            dragboard.setContent(clipboardContent);

            this.draggingTab.set(label);
            label.setStyle("-fx-background-color: lightgrey;");
            event.consume();
        };

        this.onDragOver = (labelAtTab, dragEvent) -> {
            final Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasString()
                    && TAB_DRAG_KEY.equals(dragboard.getString())
                    && this.draggingTab.get() != null) {
                dragEvent.acceptTransferModes(TransferMode.MOVE);
                dragEvent.consume();
            }
        };

        this.onDragDropped = (labelAtTab, dragEvent) -> {
            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                Label draggedTab = (Label) dragEvent.getGestureSource();
                try {
                    int tabInsertIndex = this.tabsPanel.getChildren().indexOf(labelAtTab.label());

                    this.draggableTabLabels.remove(draggedTab);
                    this.draggableTabLabels.add(tabInsertIndex, draggedTab);
                    draggedTab.setStyle("-fx-background-color: yellow;");

                    this.tabsPanel.getChildren().setAll(this.draggableTabLabels);
                    this.tabsPanel.getChildren().add(this.add);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                success = true;
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        };
    }

    public void add(
            Tab tab,
            Function<Tab, DirectoryAtTabTreeItem> assignRootTreeItemToTab,
            BiConsumer<LabelAtTab, MouseEvent> onTabSelected) {
        DirectoryAtTabTreeItem rootTreeItem = assignRootTreeItemToTab.apply(tab);
        LabelAtTab labelAtTab = new LabelAtTab(tab, rootTreeItem);
        Label label = labelAtTab.label();

        label.setOnMouseClicked(event -> onTabSelected.accept(labelAtTab, event));
        label.setOnDragOver(event -> onDragOver.accept(labelAtTab, event));
        label.setOnDragDropped(event -> onDragDropped.accept(labelAtTab, event));
        label.setOnDragDetected(event -> onDragDetected.accept(labelAtTab, event));

        this.draggableTabLabels.add(label);

        this.tabsPanel.getChildren().add(label);
    }

    public VBox tabsPanel() {
        return tabsPanel;
    }
}
