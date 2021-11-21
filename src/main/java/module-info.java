module diarsid.navigator {

    requires java.desktop;
    requires javafx.controls;
    requires javafx.swing;
    requires org.slf4j;
    requires diarsid.filesystem;
    requires diarsid.support;
    requires diarsid.support.javafx;

    opens diarsid.navigator to
            javafx.graphics,
            javafx.base;

    opens diarsid.navigator.view.table to
            javafx.base;
}
