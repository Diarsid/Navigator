package diarsid.navigator.view.table;

import diarsid.support.objects.references.impl.PossibleListenable;

import static java.util.Objects.nonNull;

import static diarsid.support.objects.references.impl.References.listenablePossibleButEmpty;

public class SingleEditingPerTable {

    private final PossibleListenable<FilesTableCellForName> editingCell;

    public SingleEditingPerTable() {
        this.editingCell = listenablePossibleButEmpty();
    }

    boolean isInProcess() {
        return this.editingCell.isPresent();
    }

    boolean isNotInProcess() {
        return this.editingCell.isNotPresent();
    }

    boolean isEditing(FilesTableCellForName cell) {
        return this.editingCell.equalsTo(cell);
    }

    void startWith(FilesTableCellForName cell) {
        FilesTableCellForName old = this.editingCell.resetTo(cell);
        if ( nonNull(old) ) {
            old.cancelEdit();
        }
    }

    void cancel() {
        this.editingCell.ifPresent(FilesTableCellForName::cancelEdit);
        this.editingCell.nullify();
    }

}
