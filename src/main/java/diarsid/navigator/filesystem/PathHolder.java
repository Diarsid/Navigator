package diarsid.navigator.filesystem;

import java.nio.file.Path;

interface PathHolder {

    Path nioPath();

    void movedTo(Path newPath);

    void contentChanged();
}
