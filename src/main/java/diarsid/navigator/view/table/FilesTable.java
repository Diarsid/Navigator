package diarsid.navigator.view.table;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;

import diarsid.filesystem.api.Directory;
import diarsid.filesystem.api.FSEntry;
import diarsid.filesystem.api.FileSystem;
import diarsid.navigator.view.ViewComponent;
import diarsid.navigator.view.dragdrop.DragAndDropObjectTransfer;
import diarsid.navigator.view.fsentry.contextmenu.FSEntryContextMenuFactory;
import diarsid.navigator.view.icons.Icons;
import diarsid.support.javafx.ClickOrDragDetector;
import diarsid.support.javafx.FrameSelection;
import diarsid.support.objects.references.Possible;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toList;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.layout.Priority.ALWAYS;

import static diarsid.support.objects.references.References.simplePossibleButEmpty;


public class FilesTable implements ViewComponent {

    private final Icons icons;
    private final TableView<FilesTableItem> tableView;
    private final FrameSelection selection;
    private final DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles;
    private final FSEntryContextMenuFactory contextMenuFactory;
    private final Possible<Directory> directory;
    private final Consumer<FilesTableItem> onItemInvoked;
    private final SingleEditingPerTable editing;
    private final FilesTableFrameSelectionDragScrollListener selectionListener;
//    private final Set<Integer> selectedIndiciesCopy;
    private final Object tableLock;

    private final AtomicInteger scrollSelectionMaxIndex;
    private final AtomicInteger scrollSelectionMinIndex;

    private boolean doSelectedIndiciesCopy = true;

    public FilesTable(
            FileSystem fileSystem,
            FSEntryContextMenuFactory contextMenuFactory,
            Icons icons,
            FrameSelection frameSelection,
            Consumer<FilesTableItem> onItemInvoked,
            BiConsumer<FSEntry, String> onRename,
            DragAndDropObjectTransfer<List<FSEntry>> dragAndDropFiles) {
        this.icons = icons;
        this.tableView = new TableView<>();
        this.selection = frameSelection;
        this.dragAndDropFiles = dragAndDropFiles;
        this.onItemInvoked = onItemInvoked;
        this.directory = simplePossibleButEmpty();

        this.scrollSelectionMaxIndex = new AtomicInteger(-1);
        this.scrollSelectionMinIndex = new AtomicInteger(-1);

        this.contextMenuFactory = contextMenuFactory;

        this.tableView.setContextMenu(this.contextMenuFactory.createNewFor(this.directory));
        this.tableView.setPlaceholder(new Label("empty directory"));

        TableColumn<FilesTableItem, ImageView> columnIcons = new TableColumn<>();
        TableColumn<FilesTableItem, String> columnNames = new TableColumn<>("Name");
        TableColumn<FilesTableItem, String> columnTypes = new TableColumn<>("Type");
        TableColumn<FilesTableItem, String> columnSizes = new TableColumn<>("Size");

        this.editing = new SingleEditingPerTable();

        columnIcons.setCellFactory(column -> new FilesTableCellForIcon());
        columnNames.setCellFactory(column -> new FilesTableCellForName(onRename));
        columnTypes.setCellFactory(column -> new FilesTableCellForExtType());
        columnSizes.setCellFactory(column -> new FilesTableCellForSize());

        columnIcons.setCellValueFactory(new PropertyValueFactory<>("icon"));
        columnNames.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnTypes.setCellValueFactory(new PropertyValueFactory<>("extType"));
        columnSizes.setCellValueFactory(new PropertyValueFactory<>("sizeFormat"));

        columnIcons.setResizable(false);

        columnIcons.setReorderable(false);
        columnNames.setReorderable(false);
        columnTypes.setReorderable(false);
        columnSizes.setReorderable(false);

        columnNames.setEditable(true);
        columnNames.setOnEditCommit(event -> {
            int a = 5;
        });

        columnIcons.minWidthProperty().bind(this.icons.iconSize().add(10));
        columnIcons.maxWidthProperty().bind(this.icons.iconSize().add(10));
        columnIcons.setSortable(false);

        this.tableView.getColumns().add(columnIcons);
        this.tableView.getColumns().add(columnNames);
        this.tableView.getColumns().add(columnTypes);
        this.tableView.getColumns().add(columnSizes);

        columnNames.prefWidthProperty().bind(this.tableView.widthProperty().subtract(columnIcons.widthProperty()).multiply(0.7));
        columnTypes.prefWidthProperty().bind(this.tableView.widthProperty().subtract(columnIcons.widthProperty()).multiply(0.15));
        columnSizes.prefWidthProperty().bind(this.tableView.widthProperty().subtract(columnIcons.widthProperty()).multiply(0.1));

        this.tableView.getSelectionModel().setSelectionMode(MULTIPLE);
//        this.selectedIndiciesCopy = new HashSet<>();
        ListChangeListener<Integer> selectionListener = change -> {
            while (change.next()) {
                if (change.wasPermutated()) {
                    for (int i = change.getFrom(); i < change.getTo(); ++i) {
                        //permutate
                    }
                } else if (change.wasUpdated()) {

                } else {
                    for (Integer removed : change.getRemoved()) {
                        System.out.println("[TABLE SELECTION] unselect " + this.tableView.getItems().get(removed).fsEntry().name());
//                        if ( doSelectedIndiciesCopy ) {
//                            System.out.println("[TABLE SELECTION] unselect copy");
//                            this.selectedIndiciesCopy.remove(removed);
//                        }
                    }
                    for (Integer added : change.getAddedSubList()) {
                        System.out.println("[TABLE SELECTION] select   " + this.tableView.getItems().get(added).fsEntry().name());
//                        if ( doSelectedIndiciesCopy ) {
//                            System.out.println("[TABLE SELECTION] select copy");
//                            this.selectedIndiciesCopy.add(added);
//                        }
                    }
                }
            }
        };
//        this.tableView.getSelectionModel().getSelectedIndices().addListener(selectionListener);

//        this.tableView.setOnScroll(scrollEvent -> {
//            double sceneMouseX = scrollEvent.getSceneX();
//            double sceneMouseY = scrollEvent.getSceneY();
//            double x = scrollEvent.getDeltaX();
//            double y = scrollEvent.getDeltaY();
//            System.out.println("scroll table x: " + x + ", y: " + y);
//            this.selection.scrolled(sceneMouseX, sceneMouseY, x, y);
//        });

        Platform.runLater(() -> {
                    ScrollBar scrollBar = (ScrollBar) this.tableView.lookup(".scroll-bar:vertical");

                    scrollBar.getUnitIncrement();
                    scrollBar.getBlockIncrement();
                    scrollBar.getVisibleAmount();

                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if ((Double) newValue == 1.0) {
                            System.out.println("Bottom!");
                        } else if ((Double) newValue == 0.0) {
                            System.out.println("Top!");
                        }
                        System.out.println(format(""));
                    });

                });

        this.selectionListener = new FilesTableFrameSelectionDragScrollListener(
                this::isDraggingAllowed, this.tableView, this.selection, this.dragAndDropFiles);

        ClickOrDragDetector clickOrDragDetector = ClickOrDragDetector.Builder
                .createFor(this.tableView)
                .withOnClickNotDrag(mouseEvent -> {
                    System.out.println("table clicked");
                })
                .withOnDragNotClick(this.selectionListener)
                .withPressedDurationThreshold(200)
                .build();

        this.tableView.setRowFactory(this::newTableRow);

        HBox.setHgrow(this.tableView, ALWAYS);

        fileSystem.changes().listenForEntriesAdded(this::onEntriesAdded);
        fileSystem.changes().listenForEntriesRemoved(this::onEntriesRemoved);

        this.tableLock = new Object();
    }

    private boolean isDraggingAllowed() {
        return this.editing.isNotInProcess();
    }

    private FilesTableItem createItem(FSEntry fsEntry) {
        return new FilesTableItem(this.icons, fsEntry);
    }

    private void onEntriesAdded(List<FSEntry> fsEntries) {
        if ( this.directory.isNotPresent() ) {
            return;
        }

        Directory directory = this.directory.orThrow();

        List<FSEntry> entries = new ArrayList<>();

        for ( FSEntry addedEntry : fsEntries ) {
            if ( directory.isDirectParentOf(addedEntry) ) {
                entries.add(addedEntry);
            }
        }

        System.out.println("[TABLE ADD] " + entries);
        this.addAll(entries);
    }

    private void addAll(List<FSEntry> entries) {
        synchronized ( this.tableLock ) {
//            System.out.println("[TABLE SELECTION] restore selection: " + this.selectedIndiciesCopy);
            this.doSelectedIndiciesCopy = false;

            FilesTableItem item;
            for ( FSEntry entry : entries ) {
                if ( this.contains(entry) ) {
                    continue;
                }
                item = this.createItem(entry);
                this.tableView.getItems().add(item);
            }
            Collections.sort(this.tableView.getItems());

            this.tableView.getSelectionModel().clearSelection();
//            this.selectedIndiciesCopy.forEach(this::selectIndex);
            this.doSelectedIndiciesCopy = true;
            System.out.println("[TABLE SELECTION] restore selection stop");
        }
    }

    private boolean contains(FSEntry fsEntry) {
        return this.tableView.getItems().stream().anyMatch(item -> item.is(fsEntry));
    }

    private void onEntriesRemoved(List<Path> paths) {
        if ( this.directory.isNotPresent() ) {
            return;
        }

        Directory directory = this.directory.orThrow();

        List<Path> pathsToRemove = new ArrayList<>();

        for ( Path removedPath : paths ) {
            if ( directory.has(removedPath) ) {
                this.clear();
            }
            else if ( directory.isDirectParentOf(removedPath) ) {
                pathsToRemove.add(removedPath);
            }
        }

        if ( pathsToRemove.isEmpty() ) {
            return;
        }

        this.removeAllPaths(pathsToRemove);
    }

    public void clear() {
        synchronized ( this.tableLock ) {
            this.tableView.getItems().clear();
        }

        if ( this.directory.isPresent() ) {
            this.directory.nullify();
        }

        this.editing.cancel();
    }

    public void show(Directory newDirectory) {
        boolean sameDirectory = this.directory.equalsTo(newDirectory);
        this.editing.cancel();
        this.directory.resetTo(newDirectory);
        List<FSEntry> children = new ArrayList<>();
        long start = currentTimeMillis();
        newDirectory.feedChildren(children::addAll);
        long stop = currentTimeMillis();
        System.out.println("[TABLE] [feed] " + (stop - start));
        this.set(children);
        if ( this.tableView.getItems().size() > 0 ) {
            this.tableView.scrollTo(0);
        }

        if ( sameDirectory ) {
//            System.out.println("[TABLE SELECTION] restore selection: " + this.selectedIndiciesCopy);
            this.doSelectedIndiciesCopy = false;
//            this.selectedIndiciesCopy.forEach(this::selectIndex);
            this.doSelectedIndiciesCopy = true;
            System.out.println("[TABLE SELECTION] restore selection stop");
        }
        else {
            this.tableView.getSelectionModel().clearSelection();
        }
    }

    private void selectIndex(Integer index) {
        System.out.println("[TABLE SELECTION] select manually " + this.tableView.getItems().get(index).fsEntry().name());
        this.tableView.getSelectionModel().select(index);
    }

    private void set(List<FSEntry> entries) {
        long start = currentTimeMillis();
        synchronized ( this.tableLock ) {
            List<FilesTableItem> items = entries
                    .stream()
                    .map(this::createItem)
                    .collect(toList());
            this.tableView.getItems().setAll(items);
        }
        long stop = currentTimeMillis();
        if ( entries.size() > 20 ) {
            System.out.println("[TABLE] [set] " + (stop - start));
        }
    }

    @Override
    public Node node() {
        return this.tableView;
    }

    public void remove(FSEntry fsEntry) {
        synchronized ( this.tableLock ) {
            Optional<FilesTableItem> fileTableItem = this.tableView
                    .getItems()
                    .stream()
                    .filter(item -> fsEntry.equals(item.fsEntry()))
                    .findFirst();

            fileTableItem.ifPresent(tableItem -> this.tableView.getItems().remove(tableItem));
        }
    }

    void removeAll(List<FSEntry> fsEntries) {
        if ( fsEntries.isEmpty() ) {
            return;
        }

        synchronized ( this.tableLock ) {
            List<FilesTableItem> itemsToRemove = this.tableView
                    .getItems()
                    .stream()
                    .filter(item -> fsEntries.contains(item.fsEntry()))
                    .collect(toList());

            if (itemsToRemove.isEmpty()) {
                return;
            }

//        List<FilesTableItem> selectedItems = this.tableView.getSelectionModel().getSelectedItems();
//        if ( selectedItems ) {
//
//        }

            this.tableView.getItems().removeAll(itemsToRemove);
        }
    }

    private void removeAllPaths(List<Path> paths) {

        if ( paths.isEmpty() ) {
            return;
        }

        synchronized ( this.tableLock ) {
            List<FilesTableItem> itemsToRemove = this.tableView
                    .getItems()
                    .stream()
                    .filter(item -> paths.contains(item.fsEntry().path()))
                    .peek(item -> System.out.println("[TABLE REMOVE] " + item.fsEntry().path()))
                    .collect(toList());

            if (itemsToRemove.isEmpty()) {
                return;
            }

//        List<FilesTableItem> selectedItems = this.tableView.getSelectionModel().getSelectedItems();
//        if ( selectedItems ) {
//
//        }

            this.tableView.getItems().removeAll(itemsToRemove);
        }
    }

    private TableRow<FilesTableItem> newTableRow(TableView<FilesTableItem> tableView) {
        return new FilesTableRow(
                this.contextMenuFactory,
                this.directory,
                this.onItemInvoked,
                this.dragAndDropFiles,
                this.editing,
                this::onRowScrolled);
    }

    private void onRowScrolled(ScrollEvent scrollEvent, FilesTableRow rowScrolled) {
        this.selectionListener.onScrolled(scrollEvent, rowScrolled);
    }
}
