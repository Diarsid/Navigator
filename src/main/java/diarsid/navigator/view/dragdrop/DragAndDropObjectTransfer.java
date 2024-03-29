package diarsid.navigator.view.dragdrop;

import java.util.Map;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.text.Text;

import diarsid.support.objects.references.Possible;

import static java.util.Objects.isNull;
import static javafx.scene.input.TransferMode.MOVE;

import static diarsid.support.objects.references.References.simplePossibleButEmpty;

public class DragAndDropObjectTransfer<T> {

    private final String dragboardKey;
    private final Map<Class<? extends Node>, String> javaClassesAndStyleClasses;
    private final Possible<T> whatToDrag;

    public DragAndDropObjectTransfer(
            String dragboardKey,
            Map<Class<? extends Node>, String> javaClassesAndStyleClasses) {
        this.dragboardKey = dragboardKey;
        this.javaClassesAndStyleClasses = javaClassesAndStyleClasses;
        this.whatToDrag = simplePossibleButEmpty();
    }

    public void startDragAndDrop(Node node, T whatToDrag) {
        Dragboard dragboard = node.startDragAndDrop(MOVE);
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(this.dragboardKey);
        dragboard.setContent(clipboardContent);
        this.whatToDrag.resetTo(whatToDrag);
    }

    public boolean isDragAcceptable(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        if ( ! dragboard.hasString() ) {
            return false;
        }

        if ( ! dragboard.getString().equalsIgnoreCase(this.dragboardKey) ) {
            return false;
        }

        Object target = dragEvent.getGestureTarget();

        if ( isNull(target) || ! (target instanceof Node) ) {
            target = dragEvent.getTarget();
        }

        if ( ! (target instanceof Node) ) {
            return false;
        }

        Node targetNode = (Node) target;

        if ( target instanceof Text ) {
            targetNode = targetNode.getParent();
        }

        String styleClass = this.javaClassesAndStyleClasses.get(targetNode.getClass());

        if ( isNull(styleClass) ) {
            return false;
        }

        boolean hasStyleClass = targetNode.getStyleClass().contains(styleClass);

        boolean acceptable = hasStyleClass && this.whatToDrag.isPresent();

        return acceptable;
    }

    public boolean isDropAcceptable(DragEvent dragEvent) {
        return this.isDragAcceptable(dragEvent);
    }

    public T get() {
        return this.whatToDrag.orThrow();
    }

}
