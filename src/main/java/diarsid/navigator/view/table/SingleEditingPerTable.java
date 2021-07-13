package diarsid.navigator.view.table;

import diarsid.support.objects.references.PossibleProperty;

import static java.util.Objects.nonNull;

import static diarsid.support.objects.references.References.possiblePropertyButEmpty;


public class SingleEditingPerTable {

    private final PossibleProperty<FilesTableCellForName> editingCell;

    public SingleEditingPerTable() {
        this.editingCell = possiblePropertyButEmpty();
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
