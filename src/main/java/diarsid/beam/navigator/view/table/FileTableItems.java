package diarsid.beam.navigator.view.table;

import java.util.HashMap;
import java.util.Map;

import diarsid.beam.navigator.filesystem.FSEntry;
import diarsid.beam.navigator.view.icons.Icons;

class FileTableItems {

    private final Icons icons;
    private final Map<FSEntry, FileTableItem> itemsByEntries;

    public FileTableItems(Icons icons) {
        this.icons = icons;
        this.itemsByEntries = new HashMap<>();
    }

    FileTableItem getFor(FSEntry fsEntry) {
        return this.itemsByEntries.computeIfAbsent(
                fsEntry,
                newFsEntry -> new FileTableItem(this.icons, newFsEntry));
    }
}
