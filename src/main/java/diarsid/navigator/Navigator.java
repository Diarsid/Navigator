package diarsid.navigator;

import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FileSystem;

public class Navigator {

    private final FileSystem fileSystem;
    private final NavigatorView navigatorView;

    public Navigator() {
        this.fileSystem = FileSystem.INSTANCE;
        AtomicReference<NavigatorView> viewRef = new AtomicReference<>();
        CountDownLatch lock = new CountDownLatch(1);

        Platform.startup(() -> {
            viewRef.set(new NavigatorView());
            lock.countDown();
        });

        try {
            lock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }

        this.navigatorView = viewRef.get();
    }

    public void open(String path) {
        Directory directory = this.fileSystem.toDirectory(Paths.get(path));
        Platform.runLater(() -> this.navigatorView.open(directory));
    }

    public void open(Directory directory) {
        Platform.runLater(() -> this.navigatorView.open(directory));
    }
}
