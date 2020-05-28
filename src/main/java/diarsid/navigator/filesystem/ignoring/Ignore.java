package diarsid.navigator.filesystem.ignoring;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import diarsid.navigator.filesystem.FSEntry;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

public class Ignore {

    private final UUID uuid;
    private final FSEntry fsEntry;
    private final LocalDateTime time;

    Ignore(FSEntry fsEntry) {
        this.uuid = randomUUID();
        this.fsEntry = fsEntry;
        this.time = now();
    }

    public UUID uuid() {
        return this.uuid;
    }

    public FSEntry fsEntry() {
        return this.fsEntry;
    }

    public LocalDateTime time() {
        return this.time;
    }

    public boolean isDirectory() {
        return this.fsEntry.isDirectory();
    }

    public boolean isFile() {
        return this.fsEntry.isFile();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ignore)) return false;
        Ignore ignore = (Ignore) o;
        return uuid.equals(ignore.uuid) &&
                fsEntry.equals(ignore.fsEntry) &&
                time.equals(ignore.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, fsEntry, time);
    }
}
