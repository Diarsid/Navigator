package diarsid.navigator.view.table;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import diarsid.files.Extension;
import diarsid.filesystem.api.FSEntry;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.objects.references.Possible;

import static diarsid.navigator.Navigator.NAVIGATOR_THREADS;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;


public class FilesTableItem implements Comparable<FilesTableItem> {

    private final Icons icons;
    private final FSEntry entry;
    private final Possible<FilesTableRow> row;
    private final ImageView iconView;

    public FilesTableItem(Icons icons, FSEntry entry) {
        this.entry = entry;
        this.icons = icons;
        this.row = simplePossibleButEmpty();
        this.iconView = new ImageView();

        ReadOnlyDoubleProperty size = this.icons.sizeProperty();
        this.iconView.fitWidthProperty().bind(size);
        this.iconView.fitHeightProperty().bind(size);
        this.iconView.setPreserveRatio(true);
        this.iconView.getStyleClass().add("icon");
        this.iconView.setImage(this.icons.getDefaultFor(this.entry).image());
        NAVIGATOR_THREADS.runNamedAsync(
                "load icon " + this.entry.name(),
                () -> {
                    Image image = this.icons.getFor(this.entry).image();
                    Platform.runLater(() -> {
                        this.iconView.setImage(image);
                    });
                });
    }

    public FSEntry fsEntry() {
        return this.entry;
    }

    public boolean is(FSEntry fsEntry) {
        return this.entry.equals(fsEntry);
    }

    public Possible<FilesTableRow> row() {
        return this.row;
    }

    public String getName() {
        return this.entry.name();
    }

    public String getSizeFormat() {
        if ( this.entry.isDirectory() ) {
            return null;
        }
        else {
            return this.entry.asFile().sizeFormat();
        }
    }

    public String getExtType() {
        if ( this.entry.isDirectory() ) {
            return "directory";
        }
        else {
            return this.entry.asFile().extension().map(Extension::name).orElse("");
        }
    }

    public ImageView getIcon() {
        return this.iconView;
    }

    @Override
    public int compareTo(FilesTableItem other) {
        return this.entry.compareTo(other.entry);
    }
}
