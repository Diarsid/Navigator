package diarsid.beam.navigator.view.table;

import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static diarsid.beam.navigator.View.VIEW_GROUP;

class FilesTableFrameSelection {

    private final Rectangle selection;

    private double x;
    private double y;

    public FilesTableFrameSelection() {
        this.selection = new Rectangle();
        this.selection.setHeight(0);
        this.selection.setWidth(0);
        this.selection.setVisible(false);
        this.selection.toBack();
        this.selection.mouseTransparentProperty().set(true);
        this.selection.getStyleClass().add("files-table-frame-selection");
    }

    void start(MouseEvent mouseEvent) {
        System.out.println("table drag - started");
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
        double mouseX = mouseEvent.getSceneX();
        double mouseY = mouseEvent.getSceneY();

        if ( mouseX <= this.x && mouseY <= this.y ) {
            this.selection.relocate(mouseX, mouseY);
            this.selection.setWidth(this.x - mouseX);
            this.selection.setHeight(this.y - mouseY);
        }
        else if ( mouseX > this.x && mouseY > this.y ) {
            this.selection.setWidth(mouseX - this.x);
            this.selection.setHeight(mouseY - this.y);
        }
        else if ( mouseX <= this.x && mouseY > this.y ) {
            this.selection.setWidth(this.x - mouseX);
            this.selection.setHeight(mouseY - this.y);
            this.selection.relocate(mouseX, this.y);
        }
        else if ( mouseX > this.x && mouseY <= this.y ) {
            this.selection.relocate(this.x, mouseY);
            this.selection.setWidth(mouseX - this.x);
            this.selection.setHeight(this.y - mouseY);
        }
    }

    void stop(MouseEvent mouseEvent) {
        this.selection.setHeight(0);
        this.selection.setWidth(0);
        this.selection.toBack();
        this.selection.setVisible(false);
    }

    boolean isIntersectedWith(FileTableRow row) {
        Bounds boundsS = this.selection.localToScreen(this.selection.getBoundsInLocal());
        Bounds boundsR = row.localToScreen(row.getBoundsInLocal());

        return boundsS.intersects(boundsR);
    }
}
