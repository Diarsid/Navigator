package diarsid.beam.navigator.model;

import java.util.Objects;

import diarsid.beam.navigator.filesystem.Directory;

import static java.lang.String.format;

public class DirectoryAtTab {

    private final Tab tab;
    private final Directory directory;

    DirectoryAtTab(Tab tab, Directory directory) {
        this.tab = tab;
        this.directory = directory;
        System.out.println(format("%s created - %s", this.getClass().getSimpleName(), directory.name()));
    }

    public Tab tab() {
        return tab;
    }

    public Directory directory() {
        return directory;
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
