package diarsid.beam.navigator.view.icons;

import javafx.scene.image.Image;

import diarsid.beam.navigator.filesystem.FSEntry;

public interface Icon {

    Image image();

    FSEntry owner();
}
