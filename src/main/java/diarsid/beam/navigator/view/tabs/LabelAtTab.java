package diarsid.beam.navigator.view.tabs;

import java.util.function.BiConsumer;

import diarsid.beam.navigator.model.Tab;
import diarsid.beam.navigator.view.filetree.DirectoryAtTabTreeItem;
import javafx.scene.control.Label;

public class LabelAtTab {

    private final Tab tab;
    private final Label label;
    private final DirectoryAtTabTreeItem tabTreeRoot;

    LabelAtTab(
            Tab tab,
            DirectoryAtTabTreeItem tabTreeRoot) {
        this.tab = tab;
        this.label = new Label();
        this.tabTreeRoot = tabTreeRoot;

        label.setPrefWidth(Double.POSITIVE_INFINITY);
        label.setStyle("-fx-background-color: yellow;");
        label.setText(tab.name());

        BiConsumer<String, String> onTabRenamed = (oldName, newName) -> {
            label.setText(newName);
        };

        tab.listenToRename(onTabRenamed);
    }

    public Tab tab() {
        return tab;
    }

    public Label label() {
        return label;
    }

    public DirectoryAtTabTreeItem tabTreeRoot() {
        return tabTreeRoot;
    }
}
