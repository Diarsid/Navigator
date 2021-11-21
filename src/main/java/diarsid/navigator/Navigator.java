package diarsid.navigator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import diarsid.filesystem.api.FileSystem;
import diarsid.filesystem.api.ignoring.Ignores;
import diarsid.support.concurrency.threads.NamedThreadSource;

import static java.nio.file.FileSystems.getDefault;

public class Navigator {

    private final static CountDownLatch PLATFORM_STARTUP_LOCK = new CountDownLatch(1);
    public final static NamedThreadSource NAVIGATOR_THREADS = new NamedThreadSource("diarsid.navigator");
    public final static Ignores NAVIGATOR_IGNORES = Ignores.INSTANCE;
    public final static FileSystem NAVIGATOR_FILE_SYSTEM = FileSystem.newInstance(
            NAVIGATOR_IGNORES, NAVIGATOR_THREADS, getDefault());

    static {
        Platform.startup(PLATFORM_STARTUP_LOCK::countDown);
    }

    CountDownLatch lock = new CountDownLatch(1);
    private final NavigatorView navigatorView;

    public Navigator() {
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
        this.navigatorView.openInNewTab(path);
    }

    public void openInCurrentTab(String path) {
        this.navigatorView.openInCurrentTab(path);
    }
}
