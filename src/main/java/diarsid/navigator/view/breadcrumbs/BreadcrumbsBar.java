package diarsid.navigator.view.breadcrumbs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import diarsid.support.objects.references.Possible;
import diarsid.support.objects.references.PossibleProperty;
import diarsid.support.objects.references.PresentProperty;
import diarsid.support.objects.references.Reference;

import static java.util.Objects.isNull;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.input.MouseEvent.MOUSE_EXITED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.layout.Priority.ALWAYS;

import static diarsid.navigator.view.breadcrumbs.BreadcrumbsBar.State.AS_ELEMENTS_BAR;
import static diarsid.navigator.view.breadcrumbs.BreadcrumbsBar.State.AS_STRING_PATH;
import static diarsid.support.objects.references.References.possiblePropertyButEmpty;
import static diarsid.support.objects.references.References.presentPropertyOf;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;

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
            this.t = simplePossibleButEmpty();
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
    private final Function<T, String> elementToString;
    private final Function<List<T>, String> elementsListToPath;
    private final BiConsumer<T, MouseEvent> onBreadcrumbClicked;
    private final Predicate<String> inputPathValidatorAndConsumer;
    private final HBox elementsHBox;
    private final List<T> values;
    private final List<Node> childrenSwap;
    private final ObservableList<Node> elementsBox;
    private final TextField pathField;
    private final SimplePool<Element<T>> elementsPool;
    private final SimplePool<Label> separatorsPool;
    private final PossibleProperty<Double> iconsSize;
    private final SimpleDoubleProperty iconsSizeProperty;
    private final PresentProperty<State> state;

    public BreadcrumbsBar(
            String separator,
            Function<T, String> elementToString,
            Function<List<T>, String> elementsListToPath,
            BiConsumer<T, MouseEvent> onBreadcrumbClicked,
            Predicate<String> inputPathValidatorAndConsumer) {
        this.separator = separator;
        this.elementToString = elementToString;
        this.elementsListToPath = elementsListToPath;
        this.onBreadcrumbClicked = onBreadcrumbClicked;
        this.inputPathValidatorAndConsumer = inputPathValidatorAndConsumer;

        this.elementsHBox = new HBox();
        this.elementsHBox.getStyleClass().add("breadcrumbs-bar");
        this.elementsHBox.setAlignment(CENTER_LEFT);

        this.values = new ArrayList<>();
        this.childrenSwap = new ArrayList<>();

        this.elementsBox = this.elementsHBox.getChildren();
        this.elementsPool = new SimplePool<>(this::createNewElement);

        this.separatorsPool = new SimplePool<>(this::createNewSeparatorLabel);

        this.iconsSize = possiblePropertyButEmpty();
        this.iconsSizeProperty = new SimpleDoubleProperty();
        this.iconsSize.listen((oldValue, newValue) -> this.iconsSizeProperty.set(newValue));

        this.pathField = new TextField();
        this.pathField.setEditable(true);
        this.pathField.getStyleClass().add("path");
        this.pathField.prefHeightProperty().bind(this.iconsSizeProperty);
        this.pathField.setOnAction(this::onPathEntered);
        HBox.setHgrow(this.pathField, ALWAYS);

        this.state = presentPropertyOf(AS_ELEMENTS_BAR, "BreadcrumbsBar.state");
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

    private void onPathEntered(ActionEvent event) {
        String input = this.pathField.getText();

        if ( isNull(input) || input.isBlank() ) {
            return;
        }

        String currentDisplayedPath = this.elementsListToPath.apply(this.values);

        if ( currentDisplayedPath.equalsIgnoreCase(input) ) {
            return;
        }

        this.state.resetTo(AS_ELEMENTS_BAR);

        boolean validAndConsumed = inputPathValidatorAndConsumer.test(input);
    }

    private void onStateChanged(State oldState, State newState) {
        if ( newState.equals(AS_STRING_PATH) ) {
            this.childrenSwap.addAll(this.elementsBox);
            this.elementsBox.clear();
            String path = this.elementsListToPath.apply(this.values);

            this.pathField.setText(path);
            this.elementsBox.add(this.pathField);
            this.pathField.selectAll();
            this.pathField.requestFocus();
        }
        else {
            this.pathField.clear();
            this.elementsBox.clear();
            this.elementsBox.addAll(this.childrenSwap);
            this.childrenSwap.clear();
        }
    }

    private BreadcrumbsBar.Element<T> createNewElement() {
        return new Element<>(this.iconsSizeProperty, this.elementToString, this.onBreadcrumbClicked);
    }

    private Label createNewSeparatorLabel() {
        Label separatorLabel = new Label(this.separator);
        separatorLabel.getStyleClass().add("separator");
        return separatorLabel;
    }

    public PossibleProperty<Double> size() {
        return this.iconsSize;
    }

    public void clear() {
        this.values.clear();

        if ( Platform.isFxApplicationThread() ) {
            this.clearInFXThread();
        }
        else {
            Platform.runLater(this::clearInFXThread);
        }
    }

    private void clearInFXThread() {
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
        if ( Platform.isFxApplicationThread() ) {
            this.appendToBarInFXThread(element);
        }
        else {
            Platform.runLater(() -> this.appendToBarInFXThread(element));
        }
    }

    private void appendToBarInFXThread(Element<T> element) {
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
