package diarsid.navigator.view.table;

import java.util.HashMap;
import java.util.Map;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.icons.Icons;

class FilesTableItems {

    private final Icons icons;
    private final Map<FSEntry, FilesTableItem> itemsByEntries;

    public FilesTableItems(Icons icons) {
        this.icons = icons;
        this.itemsByEntries = new HashMap<>();
    }

    FilesTableItem getFor(FSEntry fsEntry) {
        return this.itemsByEntries.computeIfAbsent(
                fsEntry,
                newFsEntry -> new FilesTableItem(this.icons, newFsEntry));
    }
}
