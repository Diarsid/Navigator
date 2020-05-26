package diarsid.beam.navigator.model;

import java.util.HashSet;
import java.util.Set;

import diarsid.support.objects.references.real.Possible;

import static diarsid.support.objects.references.real.Possibles.possibleButEmpty;

public class Tabs {

    private final Set<Tab> tabs;
    private final Possible<Tab> selectedTab;

    public Tabs() {
        this.tabs = new HashSet<>();
        this.selectedTab = possibleButEmpty();
    }

    public Tab createTab() {
        Tab tab = new Tab();
        this.tabs.add(tab);
        return tab;
    }

    public Tab select(Tab tab) {
        this.selectedTab.ifPresent(previousSelectedTab -> previousSelectedTab.active().resetTo(false));
        tab.active().resetTo(true);
        Tab unselectedTab = this.selectedTab.resetTo(tab);
        return unselectedTab;
    }

    public Possible<Tab> selected() {
        return this.selectedTab;
    }
}
