package diarsid.navigator.view.dragdrop;

import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;

import diarsid.support.objects.references.real.Possible;

import static javafx.scene.input.TransferMode.MOVE;

import static diarsid.support.objects.references.real.Possibles.possibleButEmpty;

public class DragAndDropContext<T extends Node> {

    private final String dragboardKey;
    private final Possible<T> draggedNode;

    public DragAndDropContext(String key) {
        this.dragboardKey = key;
        this.draggedNode = possibleButEmpty();
    }

    public void setTabDraggedContextTo(T t) {
        Dragboard dragboard = t.startDragAndDrop(MOVE);
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(dragboardKey);
        dragboard.setContent(clipboardContent);
        this.draggedNode.resetTo(t);
    }

    @SuppressWarnings("unchecked")
    public boolean isDragOverAcceptable(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        Object source = dragEvent.getGestureSource();

        if ( this.draggedNode.isNotPresent() ) {
            return false;
        }

        Class<?> nodeType = this.draggedNode.orThrow().getClass();

        if ( ! nodeType.isAssignableFrom(source.getClass()) ) {
            return false;
        }

        boolean dragboardKeyMatches = dragboard.hasString() && this.dragboardKey.equals(dragboard.getString());
        if ( ! dragboardKeyMatches ) {
            return false;
        }

        boolean draggedLabelIsSource = this.draggedNode.equalsTo((T) source);
        return draggedLabelIsSource;
    }

    @SuppressWarnings("unchecked")
    public boolean isDropAcceptable(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        Object source = dragEvent.getGestureSource();

        if ( this.draggedNode.isNotPresent() ) {
            return false;
        }

        Class<?> nodeType = this.draggedNode.orThrow().getClass();

        if ( ! nodeType.isAssignableFrom(source.getClass()) ) {
            return false;
        }

        boolean dragboardKeyMatches = dragboard.hasString() && this.dragboardKey.equals(dragboard.getString());
        if ( ! dragboardKeyMatches ) {
            return false;
        }

        boolean draggedLabelIsSource = this.draggedNode.equalsTo((T) source);
        return draggedLabelIsSource;
    }
}
