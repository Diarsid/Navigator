package diarsid.navigator.view.table;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.navigator.view.fsentry.contextmenu.FSEntryContextMenuFactory;
import diarsid.support.javafx.ClickTypeDetector;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_ENTERED;
import static javafx.scene.input.MouseEvent.MOUSE_EXITED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.TransferMode.MOVE;

import static diarsid.navigator.filesystem.Directory.Edit.FILLED;
import static diarsid.navigator.filesystem.ProgressTracker.DEFAULT;
import static diarsid.support.javafx.ClickType.DOUBLE_CLICK;
import static diarsid.support.javafx.ClickType.SEQUENTIAL_CLICK;
import static diarsid.support.javafx.ClickType.USUAL_CLICK;

class FilesTableRow extends TableRow<FilesTableItem> implements Supplier<FSEntry> {

    private final ClickTypeDetector clickTypeDetector;
    private final Supplier<Directory> selectedDirectory;
    private final Consumer<FilesTableItem> onItemInvoked;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;
    private final SingleEditingPerTable nameCellEditing;

    FilesTableRow(
            FSEntryContextMenuFactory contextMenuFactory,
            Supplier<Directory> selectedDirectory,
            Consumer<FilesTableItem> onItemInvoked,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles,
            SingleEditingPerTable singleEditingPerTable,
            BiConsumer<ScrollEvent, FilesTableRow> onScrolled) {
        super();
        this.dragAndDropFiles = dragAndDropFiles;
        this.nameCellEditing = singleEditingPerTable;
        this.selectedDirectory = selectedDirectory;
        this.onItemInvoked = onItemInvoked;

        super.setOnDragOver(this::onDragOver);
        super.setOnDragDropped(this::onDragDrop);
        super.setAlignment(CENTER);

        super.setEditable(true);

        super.setContextMenu(contextMenuFactory.createNewFor(this));

        super.addEventFilter(MOUSE_PRESSED, mouseEvent -> {
            if ( mouseEvent.isSecondaryButtonDown() ) {
                mouseEvent.consume();
            }
        });

        super.addEventHandler(MOUSE_PRESSED, mouseEvent -> {
            if ( super.isEmpty() ) {
                super.getTableView().getSelectionModel().clearSelection();
            }

            super.getTableView().getSelectionModel().select(super.getIndex());
        });

//        super.addEventHandler(MOUSE_ENTERED, mouseEvent -> {
//            var item = super.getItem();
//            if ( nonNull(item) ) {
//                System.out.println("     " + item.getName() + " ENTERED");
//            }
//        });
//
//        super.addEventHandler(MOUSE_DRAGGED, mouseEvent -> {
//            var item = super.getItem();
//            if ( nonNull(item) ) {
//                System.out.println("     " + item.getName() + " DRAGGED");
//            }
//        });
//
//        super.addEventHandler(MOUSE_EXITED, mouseEvent -> {
//            var item = super.getItem();
//            if ( nonNull(item) ) {
//                System.out.println("     " + item.getName() + " EXITED");
//            }
//        });

        this.clickTypeDetector = ClickTypeDetector.Builder
                .createFor(this)
                .withMillisAfterLastClickForType(DOUBLE_CLICK, 0)
                .withMillisAfterLastClickForType(SEQUENTIAL_CLICK, 200)
                .withMillisAfterLastClickForType(USUAL_CLICK, 1000)
                .withDoOn(DOUBLE_CLICK, this::doOnDoubleClick)
                .withDoOn(SEQUENTIAL_CLICK, this::doOnSequentialClick)
                .withDoOn(USUAL_CLICK, this::doOnUsualClick)
                .build();

        super.setOnScroll(scrollEvent -> {
            onScrolled.accept(scrollEvent, this);
        });
    }

    private FilesTableCellForName nameCell() {
        Node cell = super.getChildren().get(1);
        return (FilesTableCellForName) cell;
    }

    @Override
    public FSEntry get() {
        FilesTableItem item = super.getItem();
        if ( isNull(item) ) {
            return this.selectedDirectory.get();
        }
        else {
            return item.fsEntry();
        }
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

    private void doOnDoubleClick(MouseEvent mouseEvent) {
        FilesTableCellForName nameCell = this.nameCell();
        if ( this.nameCellEditing.isEditing(nameCell) ) {
            return;
        }

        if ( this.nameCellEditing.isInProcess() ) {
            this.nameCellEditing.cancel();
        }

        if ( super.isEmpty() ) {
            return;
        }

        FilesTableItem tableItem = super.getItem();
        if ( nonNull(tableItem) ) {
            this.onItemInvoked.accept(tableItem);
        }
    }

    private void doOnSequentialClick(MouseEvent mouseEvent) {
        FilesTableCellForName nameCell = this.nameCell();
        if ( this.nameCellEditing.isEditing(nameCell) ) {
            return;
        }

        if ( super.isSelected() ) {
            nameCell.startEdit();
            this.nameCellEditing.startWith(nameCell);
        }
        else if ( this.nameCellEditing.isInProcess() ) {
            this.nameCellEditing.cancel();
        }
    }

    private void doOnUsualClick(MouseEvent mouseEvent) {
        FilesTableCellForName nameCell = this.nameCell();
        if ( this.nameCellEditing.isEditing(nameCell) ) {
            return;
        }

        if ( this.nameCellEditing.isInProcess() ) {
            this.nameCellEditing.cancel();
        }
    }
}
