package diarsid.navigator.view.breadcrumbs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import diarsid.support.objects.SimplePool;
import diarsid.support.objects.StatefulClearable;
import diarsid.support.objects.references.Reference;
import diarsid.support.objects.references.impl.Possible;
import diarsid.support.objects.references.impl.PossibleListenable;
import diarsid.support.objects.references.impl.PresentListenable;

import static java.util.stream.Collectors.joining;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.input.MouseEvent.MOUSE_EXITED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.layout.Priority.ALWAYS;

import static diarsid.navigator.view.breadcrumbs.BreadcrumbsBar.State.AS_ELEMENTS_BAR;
import static diarsid.navigator.view.breadcrumbs.BreadcrumbsBar.State.AS_STRING_PATH;
import static diarsid.support.objects.references.impl.References.listenable;
import static diarsid.support.objects.references.impl.References.listenablePresent;
import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class BreadcrumbsBar<T> {

    enum State {
        AS_ELEMENTS_BAR,
        AS_STRING_PATH
    }

    static class Element<T> extends Parent implements StatefulClearable {

        private final HBox box;
        private final ImageView imageView;
        private final Label label;
        private final Possible<T> t;
        private final Function<T, String> valueToString;
        private final BiConsumer<T, MouseEvent> onElementClicked;

        public Element(
                SimpleDoubleProperty iconsSizeProperty,
                Function<T, String> valueToString,
                BiConsumer<T, MouseEvent> onElementClicked) {
            this.t = possibleButEmpty();
            this.valueToString = valueToString;
            this.onElementClicked = onElementClicked;

            this.box = new HBox();
            this.box.setAlignment(CENTER_LEFT);
            this.box.getStyleClass().add("element");

            this.imageView = new ImageView();
            this.imageView.fitWidthProperty().bind(iconsSizeProperty);
            this.imageView.fitHeightProperty().bind(iconsSizeProperty);

            this.label = new Label();
            this.label.setAlignment(CENTER_LEFT);

            this.box.getChildren().addAll(this.imageView, this.label);

            super.getChildren().add(this.box);

            this.box.addEventHandler(MOUSE_PRESSED, this::onMousePressedOnElement);
        }

        private void onMousePressedOnElement(MouseEvent mouseEvent) {
            if ( this.t.isPresent() ) {
                this.onElementClicked.accept(this.t.orThrow(), mouseEvent);
            }
            mouseEvent.consume();
        }

        void set(T newT) {
            this.t.resetTo(newT);
            this.label.setText(this.valueToString.apply(newT));
        }

        void set(Image icon, T newT) {
            this.t.resetTo(newT);
            this.label.setText(this.valueToString.apply(newT));
            this.imageView.setImage(icon);
        }

        @Override
        public void clear() {
            System.out.println("CLEAR ELEMENT " + this.t);
            this.t.nullify();
            this.label.setText(null);
            this.imageView.setImage(null);
        }

        @Override
        public Node getStyleableNode() {
            return this.box;
        }

    }

    private final String separator;
    private final Function<T, String> elementsToString;
    private final BiConsumer<T, MouseEvent> onBreadcrumbClicked;
    private final HBox elementsHBox;
    private final List<T> values;
    private final List<Node> childrenSwap;
    private final ObservableList<Node> elementsBox;
    private final SimplePool<Element<T>> elementsPool;
    private final SimplePool<Label> separatorsPool;
    private final PossibleListenable<Double> iconsSize;
    private final SimpleDoubleProperty iconsSizeProperty;
    private final PresentListenable<State> state;

    public BreadcrumbsBar(
            String separator,
            Function<T, String> elementsToString,
            BiConsumer<T, MouseEvent> onBreadcrumbClicked) {
        this.separator = separator;
        this.elementsToString = elementsToString;
        this.onBreadcrumbClicked = onBreadcrumbClicked;

        this.elementsHBox = new HBox();
        this.elementsHBox.getStyleClass().add("breadcrumbs-bar");
        this.elementsHBox.setAlignment(CENTER_LEFT);

        this.values = new ArrayList<>();
        this.childrenSwap = new ArrayList<>();

        this.elementsBox = this.elementsHBox.getChildren();
        this.elementsPool = new SimplePool<>(this::createNewElement);
        this.separatorsPool = new SimplePool<>(this::createNewSeparatorLabel);

        this.iconsSize = listenable(possibleButEmpty());
        this.iconsSizeProperty = new SimpleDoubleProperty();
        this.iconsSize.listen((oldValue, newValue) -> this.iconsSizeProperty.set(newValue));

        this.state = listenablePresent(AS_ELEMENTS_BAR, "BreadcrumbsBar.state");
        this.state.listen(this::onStateChanged);

        this.elementsHBox.addEventHandler(MOUSE_PRESSED, this::onMousePressedOnFreeSpace);
        this.elementsHBox.addEventHandler(MOUSE_EXITED, this::onMouseExitedFromBar);
    }

    private void onMousePressedOnFreeSpace(MouseEvent mouseEvent) {
        this.state.resetTo(AS_STRING_PATH);
    }

    private void onMouseExitedFromBar(MouseEvent mouseEvent) {
        this.state.resetTo(AS_ELEMENTS_BAR);
    }

    private void onStateChanged(State oldState, State newState) {
        if ( newState.equals(AS_STRING_PATH) ) {
            this.childrenSwap.addAll(this.elementsBox);
            this.elementsBox.clear();
            String path = this.values.stream().map(this.elementsToString).collect(joining("/"));
            TextField textField = new TextField(path);
            textField.setEditable(true);
            textField.getStyleClass().add("path");
            textField.prefHeightProperty().bind(this.iconsSizeProperty);
            HBox.setHgrow(textField, ALWAYS);
            this.elementsBox.add(textField);
            textField.selectAll();
        }
        else {
            this.elementsBox.clear();
            this.elementsBox.addAll(this.childrenSwap);
            this.childrenSwap.clear();
        }
    }

    private BreadcrumbsBar.Element<T> createNewElement() {
        return new Element<>(this.iconsSizeProperty, this.elementsToString, this.onBreadcrumbClicked);
    }

    private Label createNewSeparatorLabel() {
        Label separatorLabel = new Label(this.separator);
        separatorLabel.getStyleClass().add("separator");
        return separatorLabel;
    }

    public Reference<Double> size() {
        return this.iconsSize;
    }

    public void clear() {
        this.values.clear();
        Iterator<Node> elementsIterator = this.elementsBox.iterator();
        while ( elementsIterator.hasNext() ) {
            Node node = elementsIterator.next();
            this.returnElementToItsPool(node);
            elementsIterator.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private void returnElementToItsPool(Node node) {
        if ( node instanceof Label ) {
            Label separatorLabel = (Label) node;
            this.separatorsPool.takeBack(separatorLabel);
        }
        else if ( node instanceof Element ) {
            Element<T> element = (Element<T>) node;
            element.clear();
            this.elementsPool.takeBack(element);
        }
        else {
            throw new IllegalArgumentException("Unknown node type!");
        }
    }

    public void add(T value) {
        this.values.add(value);
        Element<T> element = this.elementsPool.give();
        element.set(value);
        this.appendToBar(element);
    }

    public void add(Image icon, T value) {
        this.values.add(value);
        Element<T> element = this.elementsPool.give();
        element.set(icon, value);
        this.appendToBar(element);
    }

    private void appendToBar(Element<T> element) {
        if (this.elementsBox.isEmpty()) {
            this.elementsBox.add(element);
        } else {
            this.elementsBox.addAll(this.separatorsPool.give(), element);
        }
    }

    public Node node() {
        return this.elementsHBox;
    }
}
