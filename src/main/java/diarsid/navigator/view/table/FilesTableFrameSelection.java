package diarsid.navigator.view.table;

import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import static java.util.Objects.isNull;

import static diarsid.navigator.NavigatorView.VIEW_GROUP;

class FilesTableFrameSelection {

    private final Rectangle selection;

    private double x;
    private double y;
    private Bounds bounds;

    public FilesTableFrameSelection() {
        this.selection = new Rectangle();
        this.selection.setHeight(0);
        this.selection.setWidth(0);
        this.selection.setVisible(false);
        this.selection.toBack();
        this.selection.mouseTransparentProperty().set(true);
        this.selection.getStyleClass().add("files-table-frame-selection");
    }

    void start(MouseEvent mouseEvent, Bounds bounds) {
        this.bounds = bounds;

        this.x = mouseEvent.getSceneX();
        this.y = mouseEvent.getSceneY();

        this.selection.relocate(this.x, this.y);
        this.selection.setHeight(1);
        this.selection.setWidth(1);
        this.selection.setVisible(true);
        this.selection.toFront();

        if ( ! VIEW_GROUP.getChildren().contains(this.selection) ) {
            VIEW_GROUP.getChildren().add(this.selection);
        }
    }

    void dragged(MouseEvent mouseEvent) {
        if ( isNull(this.bounds) ) {
            return;
        }
        double mouseX = mouseEvent.getSceneX();
        double mouseY = mouseEvent.getSceneY();

        double newWidth;
        double newHeight;
        double newX;
        double newY;

        if ( mouseX <= this.x && mouseY <= this.y ) {
            newWidth = this.x - mouseX;
            newHeight = this.y - mouseY;
            newX = mouseX;
            newY = mouseY;
        }
        else if ( mouseX > this.x && mouseY > this.y ) {
            newWidth = mouseX - this.x;
            newHeight = mouseY - this.y;
            newX = this.x;
            newY = this.y;
        }
        else if ( mouseX <= this.x && mouseY > this.y ) {
            newWidth = this.x - mouseX;
            newHeight = mouseY - this.y;
            newX = mouseX;
            newY = this.y;
        }
        else if ( mouseX > this.x && mouseY <= this.y ) {
            newWidth = mouseX - this.x;
            newHeight = this.y - mouseY;
            newX = this.x;
            newY = mouseY;
        }
        else {
            throw new IllegalStateException();
        }

        if ( newX < this.bounds.getMinX() ) {
            newX = this.bounds.getMinX();
            newWidth = this.x - this.bounds.getMinX();
        }
        if ( newY < this.bounds.getMinY() ) {
            newY = this.bounds.getMinY();
            newHeight = this.y - this.bounds.getMinY();
        }
        if ( newX + newWidth > this.bounds.getMaxX() ) {
            newWidth = this.bounds.getMaxX() - newX;
        }
        if ( newY + newHeight > this.bounds.getMaxY() ) {
            newHeight = this.bounds.getMaxY() - newY;
        }

        this.selection.relocate(newX, newY);
        this.selection.setWidth(newWidth);
        this.selection.setHeight(newHeight);
    }

    void stop(MouseEvent mouseEvent) {
        this.selection.setHeight(0);
        this.selection.setWidth(0);
        this.selection.toBack();
        this.selection.setVisible(false);
    }

    boolean isIntersectedWith(FilesTableRow row) {
        if ( isNull(this.bounds) ) {
            return false;
        }
        Bounds boundsS = this.selection.localToScreen(this.selection.getBoundsInLocal());
        Bounds boundsR = row.localToScreen(row.getBoundsInLocal());

        return boundsS.intersects(boundsR);
    }
}
