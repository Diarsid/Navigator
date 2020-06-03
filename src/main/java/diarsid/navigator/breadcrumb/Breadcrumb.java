package diarsid.navigator.breadcrumb;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class Breadcrumb {

    private final HBox hBox;
    private final List<String> strings;
    private final List<Label> labels;

    public Breadcrumb() {
        this.hBox = new HBox();
        this.strings = new ArrayList<>();
        this.labels = new ArrayList<>();
    }

    public void clear() {
        this.strings.clear();
        this.labels.clear();
        this.hBox.getChildren().clear();
    }

    public void add(String element) {
        this.strings.add(element);
        Label label = new Label(element);
        this.hBox.getChildren().addAll(label, separator());
    }

    public void remove() {
        ObservableList<Node> children = this.hBox.getChildren();
        if ( children.size() > 1 ) {
            this.hBox.getChildren().remove(children.size() - 1);
            this.hBox.getChildren().remove(children.size() - 1);
        }
        else {
            this.hBox.getChildren().remove(0);
        }
    }

    private static Label separator() {
        return new Label("/");
    }

    public Node node() {
        return hBox;
    }
}
