package diarsid.navigator.filesystem;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import diarsid.support.concurrency.stateful.workers.AbstractStatefulDestroyableWorker;

import static java.lang.String.format;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import static diarsid.navigator.Navigator.NAMED_THREAD_SOURCE;
import static diarsid.support.concurrency.threads.ThreadsUtil.shutdownAndWait;

public class LocalDirectoryWatcher extends AbstractStatefulDestroyableWorker {

    private static final Object LOCK = new Object();

    private final Path path;
    private final BiConsumer<WatchEvent.Kind<?>, Path> callback;
    private final ExecutorService async;
    private WatchService watchService;

    public LocalDirectoryWatcher(Path path, BiConsumer<WatchEvent.Kind<?>, Path> callback) {
        super(format("%s[%s]", LocalDirectoryWatcher.class.getSimpleName(), path));
        if ( ! Files.isDirectory(path) ) {
            throw new IllegalArgumentException();
        }
        this.path = path;
        this.callback = callback;
        this.async = NAMED_THREAD_SOURCE.newNamedFixedThreadPool(super.name(), 1);
    }

    @Override
    protected boolean doSynchronizedStartWork() {
        boolean started;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.path.register(this.watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            this.async.submit(this::asyncWatching);
            started = true;
        }
        catch (IOException e) {
            e.printStackTrace();
            started = false;
        }
        catch (Exception e) {
            e.printStackTrace();
            started = false;
        }

        if ( started ) {
            System.out.println(this.name() + " started");
        }

        return started;
    }

    private void asyncWatching() {
        WatchKey watchKey;
        boolean valid = true;
        while ( super.isWorkingOrTransitingToWorking() && valid ) {
            try {
                watchKey = this.watchService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }

            try {
                List<WatchEvent<?>> watchEventList = watchKey.pollEvents();
                for (WatchEvent<?> watchEvent : watchEventList) {
                    Path filePath = (Path) watchEvent.context();
                    Path dir = (Path) watchKey.watchable();
                    Path path = dir.resolve(filePath).toAbsolutePath();
                    if ( watchEvent.kind().equals(ENTRY_DELETE) ) {
                        int a = 5;
                    }
                    synchronized ( LOCK ) {
                        this.callback.accept(watchEvent.kind(), path);
                    }
                }

                valid = watchKey.reset();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected boolean doSynchronizedDestroy() {
        try {
            this.watchService.close();
            shutdownAndWait(this.async);
        }
        catch (IOException e) {

        }

        System.out.println(this.name() + " destroyed");
        return true;
    }
}
