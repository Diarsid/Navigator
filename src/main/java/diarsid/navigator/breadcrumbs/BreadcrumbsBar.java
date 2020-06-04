package diarsid.navigator.breadcrumbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import diarsid.support.objects.SimplePool;
import diarsid.support.objects.references.Reference;
import diarsid.support.objects.references.impl.PossibleListenable;

import static javafx.geometry.Pos.CENTER_LEFT;

import static diarsid.support.objects.references.impl.References.listenable;
import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class BreadcrumbsBar<T> {

    private final String separator;
    private final Function<T, String> elementsToString;
    private final HBox elementsHBox;
    private final List<String> elementNames;
    private final Map<String, HBox> elementsByNames;
    private final ObservableList<Node> elementsBox;
    private final SimplePool<Label> separators;
    private final PossibleListenable<Double> iconsSize;
    private final SimpleDoubleProperty iconsSizeProperty;

    public BreadcrumbsBar(String separator, Function<T, String> elementsToString) {
        this.separator = separator;
        this.elementsToString = elementsToString;

        this.elementsHBox = new HBox();
        this.elementsHBox.getStyleClass().add("breadcrumbs-bar");
        this.elementsHBox.setAlignment(CENTER_LEFT);

        this.elementNames = new ArrayList<>();
        this.elementsByNames = new HashMap<>();
        this.elementsBox = this.elementsHBox.getChildren();
        this.separators = new SimplePool<>(this::createNewSeparatorLabel);
        this.iconsSize = listenable(possibleButEmpty());
        this.iconsSizeProperty = new SimpleDoubleProperty();
        this.iconsSize.listen((oldValue, newValue) -> this.iconsSizeProperty.set(newValue));
    }

    public Reference<Double> size() {
        return this.iconsSize;
    }

    public void clear() {
        this.elementNames.clear();
        this.elementsByNames.clear();
        this.elementsBox.clear();
    }

    public void add(T element) {
        String elementName = this.elementsToString.apply(element);

        this.elementNames.add(elementName);
        Label label = new Label(elementName);
        label.setAlignment(CENTER_LEFT);

        HBox elementBox = new HBox();
        elementBox.setAlignment(CENTER_LEFT);
        elementBox.getStyleClass().add("element");

        elementBox.getChildren().add(label);

        if ( this.elementsBox.isEmpty() ) {
            this.elementsBox.add(elementBox);
        }
        else {
            this.elementsBox.addAll(this.separators.give(), elementBox);
        }
    }

    public void add(Image icon, T element) {
        String elementName = this.elementsToString.apply(element);

        this.elementNames.add(elementName);
        Label label = new Label(elementName);
        label.setAlignment(CENTER_LEFT);

        HBox elementBox = new HBox();
        elementBox.setAlignment(CENTER_LEFT);
        elementBox.getStyleClass().add("element");

        ImageView imageView = new ImageView();
        imageView.fitWidthProperty().bind(this.iconsSizeProperty);
        imageView.fitHeightProperty().bind(this.iconsSizeProperty);
        imageView.setImage(icon);

        elementBox.getChildren().addAll(imageView, label);

        if ( this.elementsBox.isEmpty() ) {
            this.elementsBox.add(elementBox);
        }
        else {
            this.elementsBox.addAll(this.separators.give(), elementBox);
        }
    }

    public void remove() {
        if ( this.elementsBox.size() > 1 ) {
            this.elementsBox.remove(this.elementsBox.size() - 1);
            this.elementsBox.remove(this.elementsBox.size() - 1);
        }
        else {
            this.elementsBox.remove(0);
        }
    }

    public Node node() {
        return this.elementsHBox;
    }

    private Label createNewSeparatorLabel() {
        Label separatorLabel = new Label(this.separator);
        separatorLabel.getStyleClass().add("separator");
        return separatorLabel;
    }
}
