package diarsid.navigator;

import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.filesystem.FileSystem;

public class Navigator {

    private final static CountDownLatch PLATFORM_STARTUP_LOCK = new CountDownLatch(1);

    static {
        Platform.startup(PLATFORM_STARTUP_LOCK::countDown);
    }

    CountDownLatch lock = new CountDownLatch(1);
    private final FileSystem fileSystem;
    private final NavigatorView navigatorView;

    public Navigator() {
        this.fileSystem = FileSystem.INSTANCE;
        AtomicReference<NavigatorView> viewRef = new AtomicReference<>();

        if ( PLATFORM_STARTUP_LOCK.getCount() > 0 ) {
            try {
                PLATFORM_STARTUP_LOCK.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }
        }

        Platform.runLater(() -> {
            viewRef.set(new NavigatorView());
            this.lock.countDown();
        });

        try {
            this.lock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }

        this.navigatorView = viewRef.get();
    }

    public void openInNewTab(String path) {
        Directory directory = this.fileSystem.toDirectory(Paths.get(path));
        Platform.runLater(() -> this.navigatorView.openInNewTab(directory));
    }

    public void openInCurrentTab(String path) {
        Directory directory = this.fileSystem.toDirectory(Paths.get(path));
        Platform.runLater(() -> this.navigatorView.openInCurrentTab(directory));
    }

    public void openInNewTab(Directory directory) {
        Platform.runLater(() -> this.navigatorView.openInNewTab(directory));
    }

    public void openInCurrentTab(Directory directory) {
        Platform.runLater(() -> this.navigatorView.openInCurrentTab(directory));
    }
}
