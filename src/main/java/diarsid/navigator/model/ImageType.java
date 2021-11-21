package diarsid.navigator.model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.files.Extension;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import static diarsid.navigator.Navigator.NAVIGATOR_FILE_SYSTEM;

public enum ImageType {

    JPEG("jpeg", "jpg", "JPEG", "jpg"),
    PNG("png", "PNG"),
    BMP("bmp", "BMP");

    private static final Map<String, ImageType> TYPES_BY_STRINGS;

    static {
        TYPES_BY_STRINGS = new HashMap<>();
        stream(values()).forEach(imageType -> {
            imageType.extensions.forEach(extension -> {
                TYPES_BY_STRINGS.put(extension.name(), imageType);
            });
        });
    }

    private final Set<Extension> extensions;

    ImageType(String... extensions) {
        this.extensions = stream(extensions)
                .map(extensionName -> NAVIGATOR_FILE_SYSTEM.extensions().getBy(extensionName))
                .collect(toSet());
    }

    public static Optional<ImageType> findTypeIn(Path path) {
        return findTypeIn(path.getFileName());
    }

    public static Optional<ImageType> findTypeIn(String name) {
        if ( name.endsWith(".") ) {
            return Optional.empty();
        }

        int lastDotIndex = name.lastIndexOf('.');

        if ( lastDotIndex < 0 ) {
            return Optional.empty();
        }
        else {
            String substring = name.substring(lastDotIndex + 1);
            ImageType extension = TYPES_BY_STRINGS.get(substring);
            return Optional.ofNullable(extension);
        }
    }

    public String removeFrom(String fileName) {
        Optional<Extension> foundExtension = this.extensions
                .stream()
                .filter(extension -> extension.matches(fileName))
                .findFirst();

        if ( foundExtension.isPresent() ) {
            return fileName.substring(0, fileName.length() - foundExtension.get().name().length() - 1);
        }
        else {
            return fileName;
        }
    }

    public Set<Extension> extensions() {
        return this.extensions;
    }
}
