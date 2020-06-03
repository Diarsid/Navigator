package diarsid.navigator.filesystem.ignoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.navigator.filesystem.FSEntry;
import diarsid.support.objects.groups.async.AsyncConsumers;

import static java.util.Objects.isNull;

class IgnoresHolder implements Ignores {

    private final Map<FSEntry, Ignore> ignores;
    private final Map<Ignore, FSEntry> ignoredFSEntries;
    private final AsyncConsumers<Ignore> onIgnore;
    private final AsyncConsumers<Ignore> onIgnoreUndo;
    private final Set<String> predefinedIgnoredNames;
    private final Set<Path> predefinedIgnoredPaths;

    IgnoresHolder() {
        this.ignores = new HashMap<>();
        this.ignoredFSEntries = new HashMap<>();
        this.onIgnore = new AsyncConsumers<>();
        this.onIgnoreUndo = new AsyncConsumers<>();
        this.predefinedIgnoredNames = new HashSet<>();
        this.predefinedIgnoredPaths = new HashSet<>();

        try {
            Files.lines(Paths.get("home/ignores/by_names"))
                    .map(String::toLowerCase)
                    .forEach(this.predefinedIgnoredNames::add);


            Files.lines(Paths.get("home/ignores/by_paths"))
                    .map(Paths::get)
                    .forEach(this.predefinedIgnoredPaths::add);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isIgnored(FSEntry fsEntry) {
        if ( this.predefinedIgnoredNames.contains(fsEntry.name().toLowerCase()) ) {
            return true;
        }

        if ( this.predefinedIgnoredPaths.contains(fsEntry.nioPath()) ) {
            return true;
        }

        return this.ignores.containsKey(fsEntry);
    }

    @Override
    public Optional<Ignore> findFor(FSEntry fsEntry) {
        return Optional.ofNullable(this.ignores.get(fsEntry));
    }

    @Override
    public Collection<Ignore> all() {
        return this.ignores.values();
    }

    @Override
    public List<Ignore> allBy(Sort sort) {
        return null;
    }

    @Override
    public Ignore ignore(FSEntry fsEntry) {
        Ignore ignore = new Ignore(fsEntry);
        this.ignores.put(fsEntry, ignore);
        this.ignoredFSEntries.put(ignore, fsEntry);
        this.onIgnore.accept(ignore);
        return ignore;
    }

    @Override
    public boolean undo(Ignore ignore) {
        FSEntry ignoredEntry = this.ignoredFSEntries.remove(ignore);

        if ( isNull(ignoredEntry) ) {
            return false;
        }

        this.ignores.remove(ignoredEntry);

        this.onIgnoreUndo.accept(ignore);

        return true;
    }

    @Override
    public AsyncConsumers<Ignore> onIgnore() {
        return this.onIgnore;
    }

    @Override
    public AsyncConsumers<Ignore> onIgnoreUndo() {
        return this.onIgnoreUndo;
    }
}
