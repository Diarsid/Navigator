package diarsid.navigator.view.icons;

import javafx.scene.image.Image;

import diarsid.filesystem.api.FSEntry;

public interface Icon {

    Image image();

    FSEntry owner();
}
