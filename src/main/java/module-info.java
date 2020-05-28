module diarsid.navigator {

    requires java.desktop;
    requires javafx.controls;
    requires diarsid.support;
    requires diarsid.support.javafx;

    opens diarsid.navigator to
            javafx.graphics,
            javafx.base;

    opens diarsid.navigator.filesystem to
            javafx.base;

    opens diarsid.navigator.view.table to
            javafx.base;
}
