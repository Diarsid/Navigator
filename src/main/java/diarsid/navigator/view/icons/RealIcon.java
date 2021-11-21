package diarsid.navigator.view.icons;

import javafx.scene.image.Image;

import diarsid.filesystem.api.FSEntry;

class RealIcon implements Icon {

    private final Image image;
    private final FSEntry entry;

    RealIcon(Image image, FSEntry entry) {
        this.image = image;
        this.entry = entry;
    }

    @Override
    public Image image() {
        return this.image;
    }

    @Override
    public FSEntry owner() {
        return this.entry;
    }
}
