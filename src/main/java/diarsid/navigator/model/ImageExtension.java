package diarsid.navigator.model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public enum ImageExtension {

    JPEG("jpeg", "jpg", "JPEG", "jpg"),
    PNG("png", "PNG"),
    BMP("bmp", "BMP");

    private static final Map<String, ImageExtension> EXTENSIONS_BY_STRINGS;

    static {
        EXTENSIONS_BY_STRINGS = new HashMap<>();
        stream(values()).forEach(imageExtension -> {
            imageExtension.extensions.forEach(extension -> {
                EXTENSIONS_BY_STRINGS.put(extension, imageExtension);
            });
        });
    }

    private final List<String> extensions;

    ImageExtension(String... extensions) {
        this.extensions = asList(extensions);
    }

    public static Optional<ImageExtension> findExtensionIn(Path path) {
        return findExtensionIn(path.getFileName());
    }

    public static Optional<ImageExtension> findExtensionIn(String name) {
        if ( name.endsWith(".") ) {
            return Optional.empty();
        }

        int lastDotIndex = name.lastIndexOf('.');

        if ( lastDotIndex < 0 ) {
            return Optional.empty();
        }
        else {
            String substring = name.substring(lastDotIndex + 1);
            ImageExtension extension = EXTENSIONS_BY_STRINGS.get(substring);
            return Optional.ofNullable(extension);
        }
    }
}
