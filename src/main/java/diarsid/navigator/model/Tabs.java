package diarsid.navigator.model;

import java.util.HashSet;
import java.util.Set;

import diarsid.navigator.filesystem.Directory;
import diarsid.support.objects.references.Listening;
import diarsid.support.objects.references.impl.Possible;
import diarsid.support.objects.references.impl.PossibleListenable;

import static java.util.Objects.nonNull;

import static diarsid.support.objects.references.impl.References.listenable;
import static diarsid.support.objects.references.impl.References.possibleButEmpty;

public class Tabs {

    private final Identities<Tab> identities;
    private final Set<Tab> tabs;
    private final PossibleListenable<Tab> selectedTab;
//    private final Possible<Listening<DirectoryAtTab>> currentTabListening;

    public Tabs() {
        this.identities = new Identities<>(Tab.class);
        this.tabs = new HashSet<>();
        this.selectedTab = listenable(possibleButEmpty());
//        this.currentTabListening = possibleButEmpty();

//        this.selectedTab.listen(this::onSelectedTabChange);
    }

    public Tab createTab() {
        Tab tab = new Tab(this.identities.get());

        if ( this.tabs.isEmpty() ) {
            this.select(tab);
        }

        this.tabs.add(tab);
        return tab;
    }

    public Tab select(Tab tab) {
        this.selectedTab.ifPresent(previousSelectedTab -> previousSelectedTab.active().resetTo(false));
        tab.active().resetTo(true);
        Tab unselectedTab = this.selectedTab.resetTo(tab);
        return unselectedTab;
    }

    public PossibleListenable<Tab> selected() {
        return this.selectedTab;
    }

//    private void onSelectedTabChange(Tab oldTab, Tab newTab) {
//        if (nonNull(oldTab) && this.currentTabListening.isPresent()) {
//            this.currentTabListening.orThrow().cancel();
//        }
//
//        Listening<DirectoryAtTab> newListening = newTab.selectedDirectory().listen(this::onTabSelectedDirectoryChange);
//        this.selectedDirectory.resetTo(newTab.selectedDirectory());
//
//        this.currentTabListening.resetTo(newListening);
//    }
}
