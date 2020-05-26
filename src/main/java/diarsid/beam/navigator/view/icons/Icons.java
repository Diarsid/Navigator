package diarsid.beam.navigator.view.icons;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;

import diarsid.beam.navigator.filesystem.FS;
import diarsid.beam.navigator.filesystem.FSEntry;
import diarsid.support.objects.references.real.PresentListenable;

public interface Icons {

    Icons INSTANCE = new RealIcons(FS.INSTANCE);

    Icon getFor(FSEntry fsEntry);

    ReadOnlyDoubleProperty sizeProperty();

    PresentListenable<Double> size();

}
