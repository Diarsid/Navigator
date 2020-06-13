package diarsid.navigator.model;

import java.util.Objects;

import diarsid.navigator.filesystem.Directory;

public class DirectoryAtTab {

    private final Tab tab;
    private final Directory directory;

    DirectoryAtTab(Tab tab, Directory directory) {
        this.tab = tab;
        this.directory = directory;
    }

    public Tab tab() {
        return tab;
    }

    public Directory directory() {
        return directory;
    }

    public String directoryName() {
        return this.directory.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectoryAtTab)) return false;
        DirectoryAtTab that = (DirectoryAtTab) o;
        return tab.equals(that.tab) &&
                directory.equals(that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tab, directory);
    }
}
