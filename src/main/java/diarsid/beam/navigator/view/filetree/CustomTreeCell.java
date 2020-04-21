package diarsid.beam.navigator.view.filetree;

import java.util.concurrent.atomic.AtomicLong;

import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static java.lang.String.format;

public class CustomTreeCell<T> extends TreeCell<T> {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    static Image image = new Image("file:D:/DEV/1__Projects/Diarsid/IntelliJ/Research.JavaFX/src/main/resources/ico.png", true);

    private ImageView imageView;

    public CustomTreeCell() {
        super();
        System.out.println(format("%s:%s created", this.getClass().getSimpleName(), COUNTER.incrementAndGet()));
        this.imageView = new ImageView(image);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            super.setText(null);
            super.setGraphic(null);
        } else {
            super.setText(item.toString());
            super.setGraphic(imageView);
        }
    }
}
