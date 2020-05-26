package diarsid.beam.navigator;

import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import diarsid.beam.navigator.filesystem.Directory;
import diarsid.beam.navigator.filesystem.FS;

public class Navigator {

    private final FS fs;
    private final View view;

    public Navigator() {
        this.fs = FS.INSTANCE;
        AtomicReference<View> viewRef = new AtomicReference<>();
        CountDownLatch lock = new CountDownLatch(1);

        Platform.startup(() -> {
            viewRef.set(new View());
            lock.countDown();
        });

        try {
            lock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }

        this.view = viewRef.get();
    }

    public void open(String path) {
        Directory directory = this.fs.toDirectory(Paths.get(path));
        Platform.runLater(() -> this.view.open(directory));
    }

    public void open(Directory directory) {
        Platform.runLater(() -> this.view.open(directory));
    }
}
