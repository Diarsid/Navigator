package diarsid.navigator.view.table;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.view.ViewComponent;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.javafx.ClickOrDragDetector;
import diarsid.support.javafx.FrameSelection;
import diarsid.support.objects.groups.Running;
import diarsid.support.objects.references.impl.Possible;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.layout.Priority.ALWAYS;

import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class FilesTable implements ViewComponent {

    private final Icons icons;
    private final TableView<FilesTableItem> tableView;
    private final FilesTableItems filesTableItems;
    private final FrameSelection selection;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;
    private final Possible<Directory> directory;
    private final Possible<Running> directoryChangeListener;
    private final Consumer<FilesTableItem> onItemInvoked;

    public FilesTable(
            Icons icons,
            FrameSelection frameSelection,
            Consumer<FilesTableItem> onItemInvoked,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        this.icons = icons;
        this.tableView = new TableView<>();
        this.filesTableItems = new FilesTableItems(icons);
        this.selection = frameSelection;
        this.dragAndDropFiles = dragAndDropFiles;
        this.onItemInvoked = onItemInvoked;
        this.directory = possibleButEmpty();
        this.directoryChangeListener = possibleButEmpty();

        TableColumn<FilesTableItem, ImageView> columnIcons = new TableColumn<>();
        TableColumn<FilesTableItem, String> columnNames = new TableColumn<>("Name");
        TableColumn<FilesTableItem, String> columnSizes = new TableColumn<>("Size");

        columnIcons.setCellFactory(column -> new FilesTableCellForIcon());
        columnNames.setCellFactory(column -> new FilesTableCellForName());
        columnSizes.setCellFactory(column -> new FilesTableCellForSize());

        columnIcons.setCellValueFactory(new PropertyValueFactory<>("icon"));
        columnNames.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnSizes.setCellValueFactory(new PropertyValueFactory<>("sizeFormat"));

        columnIcons.setResizable(false);

        columnIcons.setReorderable(false);
        columnNames.setReorderable(false);
        columnSizes.setReorderable(false);

        columnIcons.minWidthProperty().bind(this.icons.sizeProperty().add(10));
        columnIcons.maxWidthProperty().bind(this.icons.sizeProperty().add(10));
        columnIcons.setSortable(false);

        this.tableView.getColumns().add(columnIcons);
        this.tableView.getColumns().add(columnNames);
        this.tableView.getColumns().add(columnSizes);

        columnNames.prefWidthProperty().bind(this.tableView.widthProperty().subtract(columnIcons.widthProperty()).multiply(0.8));
        columnSizes.prefWidthProperty().bind(this.tableView.widthProperty().subtract(columnIcons.widthProperty()).multiply(0.15));

        this.tableView.getSelectionModel().setSelectionMode(MULTIPLE);

        this.tableView.setOnScroll(scrollEvent -> {
            double x = scrollEvent.getDeltaX();
            double y = scrollEvent.getDeltaY();
            System.out.println("scroll x: " + x + ", y: " + y);
            this.selection.scrolled(x, y);
        });

        Platform.runLater(() -> {
                    ScrollBar scrollBar = (ScrollBar) this.tableView.lookup(".scroll-bar:vertical");

                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if ((Double) newValue == 1.0) {
                            System.out.println("Bottom!");
                        } else if ((Double) newValue == 0.0) {
                            System.out.println("Top!");
                        }
                    });
                });

        ClickOrDragDetector.DragListener dragListener = new FilesTableFrameSelectionDragListener(
                this.tableView, this.selection, this.dragAndDropFiles);

        ClickOrDragDetector clickOrDragDetector = ClickOrDragDetector.Builder
                .createFor(this.tableView)
                .withOnClickNotDrag(mouseEvent -> {
                    System.out.println("table clicked");
                })
                .withOnDragNotClick(dragListener)
                .withPressedDurationThreshold(200)
                .build();

        this.tableView.setRowFactory(this::newTableRow);

        HBox.setHgrow(this.tableView, ALWAYS);
    }

    public void clear() {
        this.tableView.getItems().clear();

        if ( this.directory.isPresent() ) {
            this.directoryChangeListener.orThrow().cancel();
            this.directoryChangeListener.nullify();
            this.directory.nullify();
        }
    }

    public void show(Directory newDirectory) {
        this.directory.resetTo(newDirectory);
        Running newListener = newDirectory.listenForContentChanges(this::onDirectoryChange);
        Running previousListener = this.directoryChangeListener.resetTo(newListener);

        if ( nonNull(previousListener) ) {
            previousListener.cancel();
        }

        newDirectory.feedChildren(this::set);
    }

    private void onDirectoryChange() {
        this.directory.orThrow().feedChildren(this::set);
    }

    private void set(List<FSEntry> entries) {
        List<FilesTableItem> items = entries
                .stream()
                .map(this.filesTableItems::getFor)
                .collect(toList());
        this.tableView.getItems().setAll(items);
    }

    @Override
    public Node node() {
        return this.tableView;
    }

    public void remove(FSEntry fsEntry) {
        Optional<FilesTableItem> fileTableItem = this.tableView
                .getItems()
                .stream()
                .filter(item -> fsEntry.equals(item.fsEntry()))
                .findFirst();

        fileTableItem.ifPresent(tableItem -> this.tableView.getItems().remove(tableItem));
    }

    private TableRow<FilesTableItem> newTableRow(TableView<FilesTableItem> tableView) {
        return new FilesTableRow(this.directory, this.onItemInvoked, this.dragAndDropFiles, this::onScrolled);
    }

    private void onScrolled(ScrollEvent scrollEvent) {
        this.selection.scrolled(scrollEvent.getDeltaX(), scrollEvent.getDeltaY());
    }
}
