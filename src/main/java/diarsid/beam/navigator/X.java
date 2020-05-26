package diarsid.beam.navigator;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class X {
    public static void main(String[] args) throws Exception {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path dirPath = Paths.get("D:/DEV/1__Projects/Diarsid/IntelliJ/BeamNavigator/src/main/resources/test/inner");
        WatchKey watchKey = dirPath.register(
                watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        while (true) {
            try {
                watchService.take();
            } catch (InterruptedException ex) {
                break;
            }

            List<WatchEvent<?>> watchEventList = watchKey.pollEvents();
            for (WatchEvent<?> watchEvent : watchEventList) {
                Path filePath = (Path) watchEvent.context();
                Path dir = (Path) watchKey.watchable();
                Path path = dir.resolve(filePath).toAbsolutePath();
                System.out.println("Event " + watchEvent.kind() + " for " + path.toString());
            }

            boolean watchKeyValid = watchKey.reset();
            if (!watchKeyValid) {
                break;
            }
        }
    }
}
