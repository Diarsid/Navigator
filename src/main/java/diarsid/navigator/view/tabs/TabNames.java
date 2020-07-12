package diarsid.navigator.view.tabs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.navigator.filesystem.Directory;
import diarsid.navigator.model.Tab;

import static java.util.Objects.nonNull;

import static diarsid.navigator.model.Tab.DEFAULT_NAME;

public class TabNames {

    private static class ParentNames {

        private final Map<Tab, Directory> parentsByTab;
        private final Map<Tab, Directory> swap;
        private final Set<String> parentNames;
        private final AtomicInteger depth;

        ParentNames() {
            this.depth = new AtomicInteger(0);
            this.parentsByTab = new HashMap<>();
            this.swap = new HashMap<>();
            this.parentNames = new HashSet<>();
        }

        void add(Tab tab, Directory parent) {
            this.parentsByTab.put(tab, parent);
            this.parentNames.add(parent.name().toLowerCase());
        }

        void clear() {
            this.parentNames.clear();
            this.parentsByTab.clear();
            this.depth.set(0);
        }

        void goDeeper() {
            this.parentNames.clear();

            this.parentsByTab.forEach((tab, directory) -> {
                directory
                        .parent()
                        .ifPresent((parent) -> {
                            this.swap.put(tab, parent);
                            this.parentNames.add(parent.name().toLowerCase());
                        });
            });

            this.parentsByTab.clear();
            this.parentsByTab.putAll(this.swap);
            this.swap.clear();

            this.depth.incrementAndGet();
        }

        boolean areNotUnique() {
            return this.parentsByTab.size() > this.parentNames.size();
        }
    }

    private final Map<String, Set<Tab>> tabsByNames;
    private final Map<Tab, Directory> parentsByTab;
    private final Set<Tab> defaultNamedTabs;
    private final ParentNames parentNames;

    public TabNames(Map<String, Set<Tab>> tabsByNames) {
        this.tabsByNames = tabsByNames;
        this.parentsByTab = new HashMap<>();
        this.defaultNamedTabs = new HashSet<>();
        this.tabsByNames.put(DEFAULT_NAME, this.defaultNamedTabs);
        this.parentNames = new ParentNames();

    }

    public void add(Tab tab) {
        tab.selectedDirectory().listen((oldDirectory, newDirectory) -> {
            String oldName;
            if ( nonNull(oldDirectory) ) {
                oldName = oldDirectory.name().toLowerCase();
            }
            else {
                oldName = null;
            }
            String newName = newDirectory.name().toLowerCase();
            this.onNameChange(tab, oldName, newName);
        });
    }

    private void onNameChange(Tab tab, String oldName, String newName) {
        Set<Tab> tabs = this.tabsByNames.get(newName);
        if ( nonNull(tabs) && ! tabs.isEmpty() ) {
//            System.out.println("duplicate: " + newName);
            tabs.add(tab);
//            tabs.forEach(duplicateTab -> {
//                System.out.println("    tab: " + duplicateTab.selectedDirectory().orThrow().directory().path());
//            });
        }
        else {
            tabs = new HashSet<>();
            tabs.add(tab);
            this.tabsByNames.put(newName, tabs);
        }
    }

    private void process(Tab tab) {
//        String name = tab.name().toLowerCase();
//        if ( name.equalsIgnoreCase(DEFAULT_NAME) ) {
//            this.defaultNamedTabs.add(tab);
//        }
//        else {
//            List<Tab> tabsWithName = this.tabsByNames.get(name);
//            if ( isNull(tabsWithName) ) {
//                tabsWithName = new ArrayList<>();
//                tabsWithName.add(tab);
//                this.tabsByNames.put(name, tabsWithName);
//            }
//            else if ( tabsWithName.isEmpty() ) {
//                tabsWithName.add(tab);
//            }
//            else {
//                tabsWithName.add(tab);
//                this.rename(tabsWithName);
//            }
//        }
    }

    private void rename(List<Tab> tabs) {
        for ( Tab tab : tabs ) {
            if ( tab.hasSelection() ) {
                tab.selectedDirectory()
                        .orThrow()
                        .parent()
                        .ifPresent(parent -> this.parentNames.add(tab, parent));
            }
        }

        while ( this.parentNames.areNotUnique() ) {
            this.parentNames.goDeeper();
        }

        tabs.forEach(tab -> {
            Directory parent = this.parentNames.parentsByTab.get(tab);
            tab.appendPathToVisibleName(parent.name());
        });
    }
}
