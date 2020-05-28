package diarsid.navigator.view.icons;

import javafx.scene.image.Image;

import diarsid.navigator.filesystem.FSEntry;

public interface Icon {

    Image image();

    FSEntry owner();
}
