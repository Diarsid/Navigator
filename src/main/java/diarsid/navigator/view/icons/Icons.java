package diarsid.navigator.view.icons;

import javafx.beans.property.ReadOnlyDoubleProperty;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.filesystem.FileSystem;
import diarsid.support.objects.references.PresentProperty;

public interface Icons {

    Icons INSTANCE = new RealIcons(FileSystem.INSTANCE);

    Icon getFor(FSEntry fsEntry);

    ReadOnlyDoubleProperty sizeProperty();

    PresentProperty<Double> size();

}
