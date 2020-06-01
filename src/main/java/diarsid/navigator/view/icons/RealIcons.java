package diarsid.navigator.view.icons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import diarsid.navigator.model.ImageExtension;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;

import diarsid.navigator.filesystem.Extension;
import diarsid.navigator.filesystem.FS;
import diarsid.navigator.filesystem.FSEntry;
import diarsid.navigator.filesystem.File;
import diarsid.support.objects.references.real.PresentListenable;

import static java.util.Objects.isNull;

import static diarsid.navigator.model.ImageExtension.findExtensionIn;
import static diarsid.support.objects.references.real.Presents.listenablePresent;

class RealIcons implements Icons {

    private final Image file;
    private final Image folder;

    private final Map<Extension, Image> imagesByExtensions;
    private final Map<Path, Image> predefinedImagesByPaths;
    private final Map<String, Image> predefinedImagesByNames;
    private final PresentListenable<Double> size;
    private final DoubleProperty sizeProperty;

    RealIcons(FS fs) {
        this.size = listenablePresent(24d, "Icons.size");
        this.sizeProperty = new SimpleDoubleProperty(this.size.get());
        this.size.listen((oldSize, newSize) -> this.sizeProperty.set(newSize));

        this.file = this.loadFrom("home/icons/by_types/file.png");
        this.folder = this.loadFrom("home/icons/by_types/folder.png");

        this.imagesByExtensions = new HashMap<>();
        this.predefinedImagesByPaths = new HashMap<>();
        this.predefinedImagesByNames = new HashMap<>();

        try {
            Files.list(Paths.get("home/icons/by_names"))
                    .forEach((path) -> {
                        try {
                            Image icon = this.loadFrom(path.toAbsolutePath().toString());
                            String file = path.getFileName().toString().toLowerCase();
                            Optional<ImageExtension> extension = findExtensionIn(file);
                            if ( extension.isPresent() ) {
                                file = file.substring(0, file.length() - extension.)
                            }
                            this.predefinedImagesByNames.put(, icon);
                        }
                        catch (IllegalArgumentException e) {
                            System.out.println("Cannot load image: " + path);
                        }
                    });

            Files.list(Paths.get("home/icons/by_extensions"))
                    .forEach((path) -> {
                        Image icon = this.loadFrom(path.toAbsolutePath().toString());
                        Extension extension = fs.extensions().getBy(path.getFileName().toString().toLowerCase());
                        this.imagesByExtensions.put(extension, icon);
                    });
        }
        catch (IOException e) {
            e.printStackTrace();
        }

//        new Thread(() -> {
//            while ( this.size.get() > 10 ) {
//                try{
//                    Thread.sleep(3000);
//                    this.size.resetTo(this.size.get() - 1);
//                    System.out.println("do resize");
//                }
//                catch (InterruptedException e) {
//
//                }
//            }
//        }).start();
    }

    private Image loadFrom(String url) {
        return new Image("file:" + url, false);
    }

    @Override
    public ReadOnlyDoubleProperty sizeProperty() {
        return this.sizeProperty;
    }

    @Override
    public Icon getFor(FSEntry fsEntry) {
        Image image;

        if ( fsEntry.isDirectory() ) {
            image = this.predefinedImagesByPaths.get(fsEntry.nioPath());

            if ( isNull(image) ) {
                image = this.predefinedImagesByNames.get(fsEntry.name().toLowerCase());
            }

            if ( isNull(image) ) {
                image = folder;
            }
        }
        else {
            File file = fsEntry.asFile();

            image = this.predefinedImagesByPaths.get(file.nioPath());

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
                image = this.file;
            }
        }

        return new RealIcon(image, fsEntry);
    }

    @Override
    public PresentListenable<Double> size() {
        return this.size;
    }
}
