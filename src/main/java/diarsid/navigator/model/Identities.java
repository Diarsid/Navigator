package diarsid.navigator.model;

import java.util.concurrent.atomic.AtomicLong;

public class Identities<T> {

    private final Class<T> type;
    private final AtomicLong serialsProvider;

    public Identities(Class<T> type) {
        this.type = type;
        this.serialsProvider = new AtomicLong(0);
    }

    public Identity<T> get() {
        return new Identity<>(this.type, this.serialsProvider.incrementAndGet());
    }
}
