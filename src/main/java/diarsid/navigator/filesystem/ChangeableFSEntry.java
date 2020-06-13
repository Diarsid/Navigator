package diarsid.navigator.filesystem;

import java.nio.file.Path;

interface ChangeableFSEntry {

    void movedTo(Path newPath);

    void contentChanged();

    void changed();
}
