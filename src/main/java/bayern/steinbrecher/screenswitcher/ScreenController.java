package bayern.steinbrecher.screenswitcher;

import javafx.fxml.FXML;
import org.jetbrains.annotations.NotNull;

public abstract class ScreenController {
    private ScreenManager screenManager;

    @NotNull
    protected ScreenManager getScreenManager() {
        if (screenManager == null) {
            throw new IllegalStateException("There is no screen manager attached");
        } else {
            return screenManager;
        }
    }

    void setScreenManager(@NotNull ScreenManager screenManager) {
        this.screenManager = screenManager;
        afterScreenManagerIsSet();
    }

    @FXML
    protected void switchToPreviousScreen() {
        getScreenManager().switchBack();
    }

    /**
     * This method is called each time the associated {@link ScreenManager} changes. Typically, this happens exactly
     * once. Especially it is called after any {@link FXML}-like {@code initialize()}-method. Thus, in contrast to such
     * {@code initialize()}-methods this function guarantees that the associated {@link ScreenManager} is set.
     */
    protected void afterScreenManagerIsSet() {
    }
}
