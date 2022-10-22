package diarsid.navigator.view.icons;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.image.Image;

import diarsid.filesystem.api.FSEntry;

import static diarsid.navigator.Navigator.NAVIGATOR_FILE_SYSTEM;

public interface Icons {

    Icons INSTANCE = new RealIcons(NAVIGATOR_FILE_SYSTEM);

    Icon getDefaultFor(FSEntry fsEntry);

    Icon getFor(FSEntry fsEntry);

    Image getDefaultImageForDirectory();

    Image getDefaultImageForFile();

    ReadOnlyDoubleProperty iconSize();

    ReadOnlyDoubleProperty iconMarginSize();

}
