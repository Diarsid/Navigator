package diarsid.navigator.view.icons;

import javafx.beans.property.ReadOnlyDoubleProperty;

import diarsid.navigator.filesystem.FileSystem;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.support.objects.references.impl.PresentListenable;

public interface Icons {

    Icons INSTANCE = new RealIcons(FileSystem.INSTANCE);

    Icon getFor(FSEntry fsEntry);

    ReadOnlyDoubleProperty sizeProperty();

    PresentListenable<Double> size();

}
