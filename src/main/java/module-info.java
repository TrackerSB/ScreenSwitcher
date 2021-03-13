module bayern.steinbrecher.ScreenSwitcher {
    exports bayern.steinbrecher.screenswitcher;

    requires bayern.steinbrecher.GenericWizard;
    requires bayern.steinbrecher.Utility;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.jetbrains.annotations;

    opens bayern.steinbrecher.screenswitcher to javafx.fxml;
}