package diarsid.navigator.filesystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static java.lang.System.currentTimeMillis;

public class ProgressTracker<T> {

    private static final long NOT_MEASURED = -1;

    public static interface ProgressConsumer<T> {
        void accept(long progressIndex, T item);
    }

    private final Runnable onStart;
    private final Consumer<T> onItemStart;
    private final ProgressConsumer<T> onItemDone;
    private final Runnable onStop;
    private final AtomicLong done;

    private long startTime;
    private long stopTime;
    private long all;

    public ProgressTracker(
            Runnable onStart,
            Consumer<T> onItemStart,
            ProgressConsumer<T> onItemDone,
            Runnable onStop) {
        this.done = new AtomicLong(0);
        this.onStart = onStart;
        this.onItemStart = onItemStart;
        this.onItemDone = onItemDone;
        this.onStop = onStop;
        this.clear();
    }

    void begin(int size) {
        this.clear();
        this.all = size;
        this.startTime = currentTimeMillis();
        this.onStart.run();
    }

    void processing(T t) {
        this.onItemStart.accept(t);
    }

    void processingDone(T t) {
        this.onItemDone.accept(this.done.getAndIncrement(), t);
    }

    void completed() {
        this.stopTime = currentTimeMillis();
        this.onStop.run();
    }

    public long all() {
        return this.all;
    }

    public void clear() {
        this.startTime = NOT_MEASURED;
        this.stopTime = NOT_MEASURED;
        this.all = 0;
        this.done.set(0);
    }
}
