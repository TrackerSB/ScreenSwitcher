package bayern.steinbrecher.screens;

import javafx.fxml.FXML;
import org.jetbrains.annotations.NotNull;

public abstract class ScreenController {
    private ScreenManager screenManager;

    @NotNull
    protected ScreenManager getScreenManager() {
        if(screenManager == null){
            throw new IllegalStateException("The main app reference was not set yet.");
        } else {
            return screenManager;
        }
    }

    public void setScreenManager(@NotNull ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @FXML
    protected void switchToPreviousScreen(){
        getScreenManager().switchBack();
    }
}
