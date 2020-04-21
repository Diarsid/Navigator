module diarsid.beam.navigator {

    requires javafx.controls;
    requires support;

    opens diarsid.beam.navigator to
            javafx.graphics,
            javafx.base;

    opens diarsid.beam.navigator.filesystem to
            javafx.base;
}
