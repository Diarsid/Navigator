package diarsid.beam.navigator.filesystem;

import java.util.HashMap;
import java.util.Optional;

import static java.util.Objects.isNull;

public class Extensions {

    private final HashMap<String, Extension> extensionsByNames;

    public Extensions() {
        this.extensionsByNames = new HashMap<>();
    }

    public Extension getBy(String name) {
        String ext = name.strip().toLowerCase();
        return this.extensionsByNames.computeIfAbsent(ext, (ext1) -> new Extension(ext));
    }

    public Optional<Extension> getFor(File file) {
        String extensionString = this.getExtensionStringFrom(file);
        if ( isNull(extensionString) || extensionString.isBlank() ) {
            return Optional.empty();
        }
        else {
            Extension extension = this.extensionsByNames.computeIfAbsent(extensionString, Extension::new);
            return Optional.of(extension);
        }
    }

    private String getExtensionStringFrom(File file) {
        int lastCommaIndex = file.name().lastIndexOf('.');

        if ( lastCommaIndex < 0 ) {
            return null;
        }
        else {
            return file.name().substring(lastCommaIndex + 1);
        }
    }
}
