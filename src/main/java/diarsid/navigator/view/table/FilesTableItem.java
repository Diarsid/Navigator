package diarsid.navigator.view.table;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.image.ImageView;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.objects.references.impl.Possible;

import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class FilesTableItem implements Comparable<FilesTableItem> {

    private final Icons icons;
    private final FSEntry entry;
    private final Possible<FilesTableRow> row;
    private final ImageView iconView;

    public FilesTableItem(Icons icons, FSEntry entry) {
        this.entry = entry;
        this.icons = icons;
        this.row = possibleButEmpty();
        this.iconView = new ImageView();

        ReadOnlyDoubleProperty size = this.icons.sizeProperty();
        this.iconView.fitWidthProperty().bind(size);
        this.iconView.fitHeightProperty().bind(size);
        this.iconView.setPreserveRatio(true);
        this.iconView.getStyleClass().add("icon");
        this.iconView.setImage(this.icons.getFor(this.entry).image());
    }

    public FSEntry fsEntry() {
        return this.entry;
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

    public ImageView getIcon() {
        return this.iconView;
    }

    @Override
    public int compareTo(FilesTableItem other) {
        System.out.println("sort");
        return this.entry.compareTo(other.entry);
    }
}
