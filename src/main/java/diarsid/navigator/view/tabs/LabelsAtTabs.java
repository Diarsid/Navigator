package diarsid.navigator.view.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import javafx.scene.control.Label;

import diarsid.navigator.model.Tab;
import diarsid.navigator.view.dragdrop.DragAndDropNodes;
import diarsid.support.objects.references.impl.Possible;

import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class LabelsAtTabs {

    private final List<Label> draggableTabLabels;
    protected final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;
    protected final DragAndDropNodes<Label> dragAndDropLabels;
    protected final Possible<Runnable> onTabsReorderedAction;

    protected final Consumer<Tab> onTabSelected;

    public LabelsAtTabs(
            Consumer<Tab> onTabSelected,
            DragAndDropNodes<Label> dragAndDropLabels,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        this.onTabSelected = onTabSelected;
        this.draggableTabLabels = new ArrayList<>();
        this.dragAndDropLabels = dragAndDropLabels;
        this.dragAndDropFiles = dragAndDropFiles;
        this.onTabsReorderedAction = possibleButEmpty();
    }

    void onTabsReordered(Runnable action) {
        this.onTabsReorderedAction.resetTo(action);
    }

    void add(Tab tab) {
        this.addInternally(tab);
    }

    void addAndSelect(Tab tab) {
        LabelAtTab labelAtTab = this.addInternally(tab);
        this.onTabSelected.accept(labelAtTab.tab());
    }

    private LabelAtTab addInternally(Tab tab) {
        LabelAtTab labelAtTab = new LabelAtTab(tab, this);

        Label label = labelAtTab.label();

        this.draggableTabLabels.add(label);

        return labelAtTab;
    }

    List<Label> tabLabels() {
        return this.draggableTabLabels;
    }

    boolean acceptDroppedOnPanel(Label label) {
        int index = this.draggableTabLabels.indexOf(label);

        if ( index < 0 ) {
            return false;
        }

        this.draggableTabLabels.remove(index);
        this.draggableTabLabels.add(label);

        return true;
    }
}
