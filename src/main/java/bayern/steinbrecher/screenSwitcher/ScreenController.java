package bayern.steinbrecher.screenSwitcher;

import javafx.fxml.FXML;
import org.jetbrains.annotations.NotNull;

public abstract class ScreenController {
    private ScreenManager screenManager;

    @NotNull
    protected ScreenManager getScreenManager() {
        if(screenManager == null){
            throw new IllegalStateException("There is no screen manager attached");
        } else {
            return screenManager;
        }
    }

    void setScreenManager(@NotNull ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @FXML
    protected void switchToPreviousScreen(){
        getScreenManager().switchBack();
    }
}
