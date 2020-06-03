package bayern.steinbrecher.screens;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenManager {
    private static final Logger LOGGER = Logger.getLogger(ScreenManager.class.getName());
    private final StackPane screenStack = new StackPane();

    public ScreenManager(@NotNull Stage screenBaseStage) {
        screenBaseStage.setScene(new Scene(screenStack));
    }

    public void switchTo(@NotNull Screen<?> nextScreen) throws ScreenSwitchFailedException {
        try {
            screenStack.getChildren().add(nextScreen.create(this));
        } catch (IOException ex) {
            throw new ScreenSwitchFailedException(ex);
        }
    }

    public void switchBack() {
        int numberOfScreens = screenStack.getChildren().size();
        if (numberOfScreens < 2) {
            LOGGER.log(Level.WARNING, "There is no previous screen to switch to");
        } else {
            screenStack.getChildren().remove(numberOfScreens - 1);
        }
    }
}
