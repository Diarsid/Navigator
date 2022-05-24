package diarsid.navigator.view.icons;

import javafx.beans.property.ReadOnlyDoubleProperty;

import diarsid.filesystem.api.FSEntry;
import diarsid.support.objects.references.PresentProperty;

import static diarsid.navigator.Navigator.NAVIGATOR_FILE_SYSTEM;

public interface Icons {

    Icons INSTANCE = new RealIcons(NAVIGATOR_FILE_SYSTEM);

    Icon getDefaultFor(FSEntry fsEntry);

    Icon getFor(FSEntry fsEntry);

    ReadOnlyDoubleProperty sizeProperty();

    PresentProperty<Double> size();

}
