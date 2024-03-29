package diarsid.navigator.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import diarsid.support.objects.references.Listening;
import diarsid.support.objects.references.PossibleProperty;

import static diarsid.support.objects.references.References.possiblePropertyButEmpty;

public class Tabs {

    private final Identities<Tab> identities;
    private final Set<Tab> tabs;
    private final PossibleProperty<Tab> selectedTab;
//    private final Possible<Listening<DirectoryAtTab>> currentTabListening;

    public Tabs() {
        this.identities = new Identities<>(Tab.class);
        this.tabs = new HashSet<>();
        this.selectedTab = possiblePropertyButEmpty();
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

    public Tab selectedTabOrThrow() {
        return this.selectedTab.orThrow();
    }

    public boolean isNotSelected(Tab otherTab) {
        return this.selectedTab.isNotPresent() || this.selectedTab.notEqualsTo(otherTab);
    }

    public Listening<Tab> listenForSelectedTabChange(BiConsumer<Tab, Tab> change) {
        Listening<Tab> listening = this.selectedTab.listen(change);
        return listening;
    }

    public boolean hasSelected() {
        return this.selectedTab.isPresent();
    }

//    public PossibleProperty<Tab> selected() {
//        return this.selectedTab;
//    }

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
