package bayern.steinbrecher.screenswitcher;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public abstract class Screen<C extends ScreenController> {

    private final URL fxmlPath;
    private final ResourceBundle bundle;

    protected Screen(@NotNull URL fxmlPath, @NotNull ResourceBundle bundle) {
        Objects.requireNonNull(fxmlPath, "The FXML path is null. Maybe the resource was not found.");
        this.fxmlPath = fxmlPath;
        this.bundle = bundle;
    }

    @NotNull
    public Parent create(@NotNull ScreenManager manager) throws ScreenCreationException {
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlPath, bundle);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException ex) {
            throw new ScreenCreationException(ex);
        }
        root.getStyleClass()
                .add("screen");
        C controller = fxmlLoader.getController();
        controller.setScreenManager(manager);
        afterControllerIsInitialized(controller);
        return root;
    }

    /**
     * Called directly after {@link ScreenController} is set up in {@link #create(ScreenManager)}. This method is meant
     * to be used for passing data to the controller which requires as soon as possible after its instantiation.
     *
     * @param controller The {@link ScreenController} which controls this {@link Screen}.
     */
    protected void afterControllerIsInitialized(@NotNull C controller) {
    }
}
