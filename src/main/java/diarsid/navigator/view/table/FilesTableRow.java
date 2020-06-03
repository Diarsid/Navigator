package diarsid.navigator.view.table;

import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.support.javafx.DoubleClickDetector;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.TransferMode.MOVE;

import static diarsid.navigator.filesystem.Directory.Edit.FILLED;
import static diarsid.navigator.filesystem.ProgressTracker.DEFAULT;

class FilesTableRow extends TableRow<FilesTableItem> {

    private final TableView<FilesTableItem> tableView;
    private final DoubleClickDetector doubleClickDetector;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;

    FilesTableRow(
            TableView<FilesTableItem> tableView,
            Consumer<FilesTableItem> onItemInvoked,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        super();
        this.tableView = tableView;
        this.dragAndDropFiles = dragAndDropFiles;
        super.setOnDragOver(this::onDragOver);
        super.setOnDragDropped(this::onDragDrop);
        super.setAlignment(CENTER);

        super.addEventFilter(MOUSE_PRESSED, mouseEvent -> {
            if ( mouseEvent.isSecondaryButtonDown() ) {
                mouseEvent.consume();
            }
        });

        super.addEventHandler(MOUSE_PRESSED, mouseEvent -> {
            if ( super.isEmpty() ) {
                tableView.getSelectionModel().clearSelection();
            }

            tableView.getSelectionModel().select(super.getIndex());
        });

        this.doubleClickDetector = DoubleClickDetector.Builder
                .createFor(this)
                .withMillisBetweenClicks(200)
                .withDoOnDoubleClick(mouseEvent -> {
                    if ( super.isEmpty() ) {
                        return;
                    }

                    FilesTableItem tableItem = super.getItem();
                    if ( nonNull(tableItem) ) {
                        onItemInvoked.accept(tableItem);
                    }
                })
                .build();
    }

    @Override
    protected void updateItem(FilesTableItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {

        } else {
            item.row().resetTo(this);
        }
    }

    private void onDragOver(DragEvent dragEvent) {
        if ( this.canBeDropped(dragEvent) ) {
            dragEvent.acceptTransferModes(MOVE);
            dragEvent.consume();
        }
    }

    private boolean canBeDropped(DragEvent dragEvent) {
        if ( this.isEmpty() ) {
            return false;
        }

        FilesTableItem item = super.getItem();

        if ( isNull(item) ) {
            return false;
        }

        FSEntry fsEntry = item.fsEntry();

        if ( fsEntry.isFile() ) {
            return false;
        }

        Directory acceptingDirectory = fsEntry.asDirectory();

        if ( acceptingDirectory.canNotBe(FILLED) ) {
            return false;
        }

        List<FSEntry> fsEntries = this.dragAndDropFiles.get();

        if ( fsEntries.size() == 1 ) {
            FSEntry draggedEntry = fsEntries.get(0);
            if ( acceptingDirectory.canHost(draggedEntry) ) {
                return true;
            }
        }
        else if ( fsEntries.size() > 1 ) {
            boolean canHostAll = fsEntries
                    .stream()
                    .allMatch(acceptingDirectory::canHost);

            if ( canHostAll ) {
                return true;
            }
        }
        else {

        }

        return false;
    }

    private void onDragDrop(DragEvent dragEvent) {
        if ( this.canBeDropped(dragEvent) ) {
            List<FSEntry> fsEntries = this.dragAndDropFiles.get();
            Directory acceptingDirectory = super.getItem().fsEntry().asDirectory();
            boolean hosted = acceptingDirectory.hostAll(fsEntries, DEFAULT);
            if ( hosted ) {
                dragEvent.setDropCompleted(true);
                dragEvent.consume();
            }
        }
    }

    @Override
    public String toString() {
        String format = this.getClass().getSimpleName() + "{_}";

        if ( super.isEmpty() ) {
            return format.replace("_", "empty");
        }

        FilesTableItem item = super.getItem();

        if ( isNull(item) ) {
            return format.replace("_", "null");
        }

        return format.replace("_", item.fsEntry().name());
    }
}
