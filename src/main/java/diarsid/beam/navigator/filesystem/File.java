package diarsid.beam.navigator.filesystem;

import java.util.Optional;

public interface File extends FSEntry {

    long size();

    Optional<Extension> extension();

    void open();

    default Long getSize() {
        return this.size();
    }

    default SizeInBytes sizeGradation() {
        return SizeInBytes.of(this.size());
    }

    default String sizeFormat() {
        return this.sizeGradation().format(this.size());
    }
}
