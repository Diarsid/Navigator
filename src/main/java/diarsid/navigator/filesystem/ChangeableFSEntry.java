package diarsid.navigator.filesystem;

interface ChangeableFSEntry extends FSEntry {

    @Override
    default LocalDirectory asDirectory() {
        return (LocalDirectory) FSEntry.super.asDirectory();
    }

    @Override
    default LocalFile asFile() {
        return (LocalFile) FSEntry.super.asFile();
    }
}
