package diarsid.navigator.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

public class Identity<T> implements Comparable<Identity<T>> {

    private final UUID uuid;
    private final Class<T> type;
    private final LocalDateTime time;
    private final long serial;

    Identity(Class<T> type, long serial) {
        this.uuid = randomUUID();
        this.type = type;
        this.time = now();
        this.serial = serial;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public LocalDateTime time() {
        return this.time;
    }

    public Class<T> type() {
        return this.type;
    }

    public long serial() {
        return this.serial;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Identity)) return false;
        Identity<?> otherIdentity = (Identity<?>) other;
        return this.uuid.equals(otherIdentity.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public int compareTo(Identity<T> other) {
        return Long.compare(this.serial, other.serial);
    }

    @Override
    public String toString() {
        return "Identity{" +
                "uuid=" + uuid +
                ", type=" + type +
                ", time=" + time +
                ", serial=" + serial +
                '}';
    }
}
