package diarsid.navigator.view.icons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;

import diarsid.files.Extension;
import diarsid.filesystem.api.FSEntry;
import diarsid.filesystem.api.File;
import diarsid.filesystem.api.FileSystem;
import diarsid.navigator.model.ImageType;
import diarsid.support.javafx.images.FilesNativeIconImageExtractor;

import static java.util.Objects.isNull;

import static diarsid.navigator.model.ImageType.findTypeIn;

class RealIcons implements Icons {

    private final Image file;
    private final Image folder;

    private final FilesNativeIconImageExtractor imageExtractor;
    private final Map<Extension, Image> imagesByExtensions;
    private final Map<Path, Image> predefinedImagesByPaths;
    private final Map<String, Image> predefinedImagesByNames;
//    private final PresentProperty<Double> size;
    private final DoubleProperty iconSize;
    private final DoubleProperty iconMarginSize;
    private final boolean extractNativeIcon = true;

    RealIcons(FileSystem fileSystem) {
        this.imageExtractor = new FilesNativeIconImageExtractor(fileSystem.extensions());
        this.iconSize = new SimpleDoubleProperty(18);
        this.iconMarginSize = new SimpleDoubleProperty(10);

        this.file = this.loadFrom("./home/icons/by_types/file.png");
        this.folder = this.loadFrom("./home/icons/by_types/folder.png");

        this.imagesByExtensions = new HashMap<>();
        this.predefinedImagesByPaths = new HashMap<>();
        this.predefinedImagesByNames = new HashMap<>();

        try (
                Stream<Path> iconsByNames = Files.list(Paths.get("./home/icons/by_names"));
                Stream<Path> iconsByExtensions = Files.list(Paths.get("./home/icons/by_extensions"))) {

            iconsByNames.forEach((path) -> {
                        try {
                            Image icon = this.loadFrom(path.toAbsolutePath().toString());
                            String fileName = path.getFileName().toString().toLowerCase();
                            Optional<ImageType> imageType = findTypeIn(fileName);
                            if ( imageType.isPresent() ) {
                                fileName = imageType.get().removeFrom(fileName);
                            }
                            this.predefinedImagesByNames.put(fileName, icon);
                        }
                        catch (IllegalArgumentException e) {
                            System.out.println("Cannot load image: " + path);
                        }
                    });


            iconsByExtensions.forEach((path) -> {
                        Image icon = this.loadFrom(path.toAbsolutePath().toString());
                        String fileName = path.getFileName().toString().toLowerCase();

                        Optional<ImageType> imageType = findTypeIn(fileName);
                        if ( imageType.isPresent() ) {
                            fileName = imageType.get().removeFrom(fileName);
                        }

                        Extension extension = fileSystem.extensions().getBy(fileName);
                        this.imagesByExtensions.put(extension, icon);
                    });

            Files.readAllLines(Paths.get("home/icons/by_paths/paths_to_icons.txt"))
                    .forEach(link -> {
                        int indexOfSeparator = link.indexOf(" // ");
                        String path = link.substring(0, indexOfSeparator).strip();
                        String iconName = link.substring(indexOfSeparator + " // ".length()).strip();
                        String iconPath = "./home/icons/by_paths/" + iconName;
                        if ( Files.exists(Paths.get(iconPath)) ) {
                            Image icon = this.loadFrom(iconPath);
                            this.predefinedImagesByPaths.put(Paths.get(path), icon);
                        }
                        else {
                            System.out.println("image '" + iconName + "' is not found for path: " + path);
                        }
                    });
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            while ( this.iconSize.get() < 30 ) {
                try{
                    Thread.sleep(3000);
                    this.iconSize.set(this.iconSize.get() + 1);
                    System.out.println("do resize");
                }
                catch (InterruptedException e) {

                }
            }
        }).start();
    }

    private Image loadFrom(String url) {
        return new Image("file:" + url, false);
    }

    @Override
    public Icon getDefaultFor(FSEntry fsEntry) {
        if ( fsEntry.isDirectory() ) {
            return new RealIcon(this.folder, fsEntry);
        }
        else {
            return new RealIcon(this.file, fsEntry);
        }
    }

    @Override
    public Icon getFor(FSEntry fsEntry) {
        Image image;

        if ( fsEntry.isDirectory() ) {
            image = this.predefinedImagesByPaths.get(fsEntry.path());

            if ( isNull(image) ) {
                image = this.predefinedImagesByNames.get(fsEntry.name().toLowerCase());
            }

            if ( isNull(image) ) {
                image = this.folder;
            }
        }
        else {
            File file = fsEntry.asFile();

            image = this.predefinedImagesByPaths.get(file.path());

            if ( isNull(image) ) {
                Optional<Extension> extension = file.extension();
                if ( extension.isPresent() ) {
                    image = this.imagesByExtensions.get(extension.get());
                }
            }

            if ( isNull(image) ) {
                image = this.predefinedImagesByNames.get(fsEntry.name().toLowerCase());
            }

            if ( isNull(image) ) {
                if ( extractNativeIcon ) {
                    image = this.imageExtractor.getFrom(
                            file.path().toFile(),
                            FilesNativeIconImageExtractor.PathCache.USE,
                            FilesNativeIconImageExtractor.ExtensionCache.NO_USE);
                }
                else {
                    image = this.file;
                }
            }
        }

        return new RealIcon(image, fsEntry);
    }

    @Override
    public Image getDefaultImageForDirectory() {
        return this.folder;
    }

    @Override
    public Image getDefaultImageForFile() {
        return this.file;
    }

    @Override
    public ReadOnlyDoubleProperty iconSize() {
        return this.iconSize;
    }

    @Override
    public ReadOnlyDoubleProperty iconMarginSize() {
        return this.iconMarginSize;
    }

}
