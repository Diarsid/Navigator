package diarsid.beam.navigator.view.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.Label;

import diarsid.beam.navigator.model.Tab;
import diarsid.beam.navigator.view.dragdrop.DragAndDropContext;
import diarsid.support.objects.references.real.Possible;

import static diarsid.support.objects.references.real.Possibles.possibleButEmpty;

public class LabelsAtTabs {

    private final List<Label> draggableTabLabels;
    protected final DragAndDropContext<Label> dragAndDropContextTab;
    protected final Possible<Runnable> onTabsReorderedAction;

    protected final Consumer<Tab> onTabSelected;

    public LabelsAtTabs(Consumer<Tab> onTabSelected, DragAndDropContext<Label> dragAndDropContextTab) {
        this.onTabSelected = onTabSelected;
        this.draggableTabLabels = new ArrayList<>();
        this.dragAndDropContextTab = dragAndDropContextTab;
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
