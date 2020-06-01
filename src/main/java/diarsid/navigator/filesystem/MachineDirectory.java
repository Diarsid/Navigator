package diarsid.navigator.filesystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import diarsid.support.objects.groups.Runnables;
import diarsid.support.objects.groups.Running;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

class MachineDirectory implements Directory {

    private final String machineName;
    private final FS fs;
    private final List<Path> roots;
    private final Runnables changeListeners;

    MachineDirectory(FS fs, Iterable<Path> rootPaths) {
        this.machineName = getMachineName();
        this.fs = fs;
        this.roots = new ArrayList<>();
        this.changeListeners = new Runnables();
        rootPaths.forEach(this.roots::add);
    }

    private static String getMachineName() {
        String machineName = "Local Machine";

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            machineName = addr.getHostName();
        }
        catch (UnknownHostException ex) {
            ex.printStackTrace();
        }

        return machineName;
    }

    @Override
    public String name() {
        return this.machineName;
    }

    @Override
    public String path() {
        return this.machineName + "/";
    }

    @Override
    public Optional<Directory> parent() {
        return Optional.empty();
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public List<Directory> parents() {
        return emptyList();
    }

    @Override
    public int depth() {
        return 0;
    }

    @Override
    public void checkChildrenPresence(Consumer<Boolean> consumer) {
        consumer.accept(true);
    }

    @Override
    public void checkDirectoriesPresence(Consumer<Boolean> consumer) {
        consumer.accept(true);
    }

    @Override
    public void checkFilesPresence(Consumer<Boolean> consumer) {
        consumer.accept(false);
    }

    @Override
    public void feedChildren(Consumer<List<FSEntry>> consumer) {
        List<FSEntry> entries = this.roots
                .stream()
                .map(this.fs::toFSEntry)
                .collect(toList());

        consumer.accept(entries);
    }

    @Override
    public void feedDirectories(Consumer<List<Directory>> consumer) {
        List<Directory> directories = this.roots
                .stream()
                .filter(this.fs::isDirectory)
                .map(this.fs::toDirectory)
                .collect(toList());

        consumer.accept(directories);
    }

    @Override
    public void feedFiles(Consumer<List<File>> consumer) {
        List<File> files = this.roots
                .stream()
                .filter(this.fs::isFile)
                .map(this.fs::toFile)
                .collect(toList());

        consumer.accept(files);
    }

    @Override
    public void host(FSEntry newEntry, Consumer<Boolean> callback) {
        throw new UnsupportedOperationException("This directory is root");
    }

    @Override
    public boolean host(FSEntry newEntry) {
        throw new UnsupportedOperationException("This directory is root");
    }

    @Override
    public Running listenForChanges(Runnable listener) {
        return this.changeListeners.add(listener);
    }

    @Override
    public boolean canBe(Directory.Edit edit) {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean moveTo(Directory newPlace) {
        throw new UnsupportedOperationException("This directory is root");
    }

    @Override
    public boolean remove() {
        throw new UnsupportedOperationException("This directory is root");
    }

    @Override
    public boolean canBeIgnored() {
        return false;
    }

    @Override
    public Path nioPath() {
        return this.roots.get(0);
    }

    @Override
    public void movedTo(Path newPath) {
        throw new UnsupportedOperationException("This directory is root");
    }

    @Override
    public void contentChanged() {
        throw new UnsupportedOperationException("This directory is root");
    }

    @Override
    public int compareTo(FSEntry otherFSEntry) {
        return 1;
    }

    List<Path> roots() {
        return this.roots;
    }
}
