package diarsid.beam.navigator.filesystem;

import java.util.Objects;

public class Extension {

    private final String name;

    public Extension(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Extension)) return false;
        Extension extension = (Extension) o;
        return name.equals(extension.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
