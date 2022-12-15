package diarsid.navigator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import diarsid.filesystem.api.FileSystem;
import diarsid.filesystem.api.ignoring.Ignores;
import diarsid.support.concurrency.threads.NamedThreadSource;
import diarsid.support.javafx.PlatformActions;

import static java.nio.file.FileSystems.getDefault;

public class Navigator {

    public final static NamedThreadSource NAVIGATOR_THREADS = new NamedThreadSource("diarsid.navigator");
    public final static Ignores NAVIGATOR_IGNORES = Ignores.INSTANCE;
    public final static FileSystem NAVIGATOR_FILE_SYSTEM = FileSystem.newInstance(
            NAVIGATOR_IGNORES, NAVIGATOR_THREADS, getDefault());


    private final NavigatorView navigatorView;

    public Navigator() {
        PlatformActions.awaitStartup();
        AtomicReference<NavigatorView> viewRef = new AtomicReference<>();

        CountDownLatch lock = new CountDownLatch(1);

        Platform.runLater(() -> {
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

    public void openInNewTab(String path) {
        this.navigatorView.openInNewTab(path);
    }

    public void openInCurrentTab(String path) {
        this.navigatorView.openInCurrentTab(path);
    }
}
