module diarsid.beam.navigator {

    requires java.desktop;
    requires javafx.controls;
    requires support;
    requires support.javafx;

    opens diarsid.beam.navigator to
            javafx.graphics,
            javafx.base;

    opens diarsid.beam.navigator.filesystem to
            javafx.base;

    opens diarsid.beam.navigator.view.table to
            javafx.base;
}
