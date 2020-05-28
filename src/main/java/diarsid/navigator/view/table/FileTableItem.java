package diarsid.navigator.view.table;

import javafx.scene.image.ImageView;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.objects.references.real.Possible;

import static diarsid.support.objects.references.real.Possibles.possibleButEmpty;

public class FileTableItem implements Comparable<FileTableItem> {

    private final Icons icons;
    private final FSEntry entry;
    private final Possible<FileTableRow> row;

    public FileTableItem(Icons icons, FSEntry entry) {
        this.entry = entry;
        this.icons = icons;
        this.row = possibleButEmpty();
    }

    public FSEntry fsEntry() {
        return this.entry;
    }

    public Possible<FileTableRow> row() {
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
        return new ImageView(this.icons.getFor(this.entry).image());
    }

    @Override
    public int compareTo(FileTableItem other) {
        System.out.println("sort");
        return this.entry.compareTo(other.entry);
    }
}
