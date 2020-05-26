package diarsid.beam.navigator.view.table;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;

import diarsid.beam.navigator.filesystem.FSEntry;
import diarsid.beam.navigator.view.ViewComponent;
import diarsid.beam.navigator.view.icons.Icons;
import diarsid.support.javafx.ClickOrDragDetector;
import diarsid.support.javafx.DoubleClickDetector;
import diarsid.support.objects.references.real.PresentListenable;

import static java.util.stream.Collectors.toList;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.layout.Priority.ALWAYS;

public class FilesTable implements ViewComponent {

    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private final Icons icons;
    private final TableView<FileTableItem> tableView;
    private final FileTableItems fileTableItems;
    private final FilesTableFrameSelection selection;

    private final AtomicInteger counter;

    private final Set<FileTableRow> rows;

    public FilesTable(Icons icons, Consumer<FileTableItem> onItemInvoked) {
        this.icons = icons;
        this.tableView = new TableView<>();
        this.fileTableItems = new FileTableItems(icons);
        this.selection = new FilesTableFrameSelection();

        this.rows = new HashSet<>();

        this.counter = new AtomicInteger();

        TableColumn<FileTableItem, ImageView> columnIcons = new TableColumn<>();
        TableColumn<FileTableItem, String> columnNames = new TableColumn<>("Name");
        TableColumn<FileTableItem, String> columnSizes = new TableColumn<>("Size");
        PresentListenable<Double> iconsSizeX = this.icons.size();

        ObjectProperty<Double> doubleProperty = new SimpleObjectProperty<>(iconsSizeX.get() + 10);
//
//        Consumer<Insets> paddingListener = (iconPadding) -> {
//            if ( counter.get() == 0 ) {
//                double sidePadding = (iconPadding.getLeft() + iconPadding.getRight()) * 2;
//                if ( sidePadding > 0 ) {
//                    Double size = iconsSizeX.get();
//                    Double newSize = size + sidePadding;
//                    System.out.println("old size " + size + " new size " + newSize);
//                    iconsSizeX.resetTo(newSize);
//
////                    columnIcons.minWidthProperty().set(newSize);
////                    columnIcons.maxWidthProperty().set(newSize);
//                    doubleProperty.set(newSize);
//                    doubleProperty.setValue(newSize);
//
//                    counter.incrementAndGet();
//                }
//            }
//        };

        columnIcons.setCellFactory(column -> new FileTableCellForIcon());
        columnNames.setCellFactory(column -> new FileTableCellForName());
        columnSizes.setCellFactory(column -> new FileTableCellForSize());

        columnIcons.setCellValueFactory(new PropertyValueFactory<>("icon"));
        columnNames.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnSizes.setCellValueFactory(new PropertyValueFactory<>("sizeFormat"));

        columnIcons.setResizable(false);

        columnIcons.minWidthProperty().bind(doubleProperty);
        columnIcons.maxWidthProperty().bind(doubleProperty);
        columnIcons.setReorderable(false);
        columnIcons.setSortable(false);

        this.tableView.getColumns().add(columnIcons);
        this.tableView.getColumns().add(columnNames);
        this.tableView.getColumns().add(columnSizes);

        this.tableView.getSelectionModel().setSelectionMode(MULTIPLE);

        ClickOrDragDetector.DragListener dragListener = new FilesTableFrameSelectionDragListener(
                this.tableView, this.selection);

        ClickOrDragDetector clickOrDragDetector = ClickOrDragDetector.Builder
                .createFor(tableView)
                .withOnClickNotDrag(mouseEvent -> {
                    System.out.println("table clicked");
                })
                .withOnDragNotClick(dragListener)
                .withPressedDurationThreshold(200)
                .build();

        this.tableView.setRowFactory(tableView -> {
            FileTableRow tableRow = new FileTableRow();

            DoubleClickDetector doubleClickDetector = DoubleClickDetector.Builder
                    .createFor(tableRow)
                    .withMillisBetweenClicks(200)
                    .withDoOnDoubleClick(mouseEvent -> {
                        FileTableItem tableItem = tableRow.getItem();
                        onItemInvoked.accept(tableItem);
                    })
                    .build();

            tableRow.setOnMouseClicked(mouseEvent -> {
                if ( tableRow.isEmpty() ) {
                    tableView.getSelectionModel().clearSelection();
                }
            });

            return tableRow;
        });

        this.tableView.setStyle("-fx-focus-color: transparent;");
        HBox.setHgrow(this.tableView, ALWAYS);
    }

    public void clear() {
        this.tableView.getItems().clear();
    }

    public void set(List<FSEntry> entries) {
        List<FileTableItem> items = entries
                .stream()
                .map(this.fileTableItems::getFor)
                .collect(toList());
        this.tableView.getItems().setAll(items);
    }

    @Override
    public Node node() {
        return this.tableView;
    }

    public void remove(FSEntry fsEntry) {
        Optional<FileTableItem> fileTableItem = tableView
                .getItems()
                .stream()
                .filter(item -> fsEntry.equals(item.fsEntry()))
                .findFirst();

        fileTableItem.ifPresent(tableItem -> tableView.getItems().remove(tableItem));
    }
}
