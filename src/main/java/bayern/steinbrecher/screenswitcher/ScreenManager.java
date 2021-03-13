package bayern.steinbrecher.screenswitcher;

import bayern.steinbrecher.javaUtility.PlatformUtility;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenManager {
    private static final Logger LOGGER = Logger.getLogger(ScreenManager.class.getName());
    private final StackPane screenStack = new StackPane();
    private final StackPane mainStack = new StackPane(screenStack);
    private final Map<Node, RunnableFuture<?>> waitForWizardEndedScreens = new HashMap<>();

    public ScreenManager(@NotNull Stage screenBaseStage) {
        screenBaseStage.setScene(new Scene(mainStack));
    }

    @NotNull
    private Parent addScreen(@NotNull Screen<?> nextScreen) throws ScreenSwitchFailedException {
        Parent content;
        try {
            content = nextScreen.create(this);
        } catch (ScreenCreationException ex) {
            throw new ScreenSwitchFailedException(ex);
        }
        try {
            PlatformUtility.runLaterBlocking(() -> screenStack.getChildren().add(content));
        } catch (Exception ex) {
            throw new ScreenSwitchFailedException("Could not show the content of the screen", ex);
        }
        return content;
    }

    public void switchTo(@NotNull Screen<?> nextScreen) throws ScreenSwitchFailedException {
        addScreen(nextScreen);
    }

    // NOTE To avoid that the caller is able to call RunnableFuture::run() only a Future object must be returned
    public <R> Future<R> switchToWithResult(@NotNull WizardScreen<R> nextScreen) throws ScreenSwitchFailedException {
        Node content = addScreen(nextScreen);
        RunnableFuture<R> callbackTask = new FutureTask<>(
                () -> nextScreen.getResult()
                        .orElseThrow(() -> new AssertionError(
                                "The callback must only be called if the result is available")));
        waitForWizardEndedScreens.put(content, callbackTask);
        return callbackTask;
    }

    public void switchBack() {
        int numberOfScreens = screenStack.getChildren().size();
        if (numberOfScreens < 2) {
            LOGGER.log(Level.WARNING, "There is no previous screen to switch to");
        } else {
            Node removedContent = screenStack.getChildren().remove(numberOfScreens - 1);
            RunnableFuture<?> callbackTask = waitForWizardEndedScreens.remove(removedContent);
            if (callbackTask != null) {
                callbackTask.run();
            }
        }
    }

    /**
     * NOTE There is no specific design applied to the overlay. Currently the JavaFx default design provides black text
     * on a black background.
     * @since 0.2.1
     */
    public void showOverlay(@NotNull String message) {
        if (isOverlayActive()) {
            final Optional<Text> overlayMessage = mainStack.getChildren()
                    .stream()
                    .filter(c -> c instanceof Text)
                    .map(c -> (Text) c)
                    .findFirst();
            if (overlayMessage.isPresent()) {
                overlayMessage.get()
                        .setText(message);
            } else {
                LOGGER.log(Level.WARNING, "Could not change overlay text");
            }
        } else {
            Rectangle overlayBackground = new Rectangle();
            overlayBackground.widthProperty()
                    .bind(mainStack.widthProperty());
            overlayBackground.heightProperty()
                    .bind(mainStack.heightProperty());
            overlayBackground.getStyleClass()
                    .add("screen-overlay");

            Text overlayMessage = new Text(message);
            overlayMessage.getStyleClass()
                    .add("screen-overlay-text");

            try {
                PlatformUtility.runLaterBlocking(
                        () -> mainStack.getChildren()
                                .addAll(overlayBackground, overlayMessage));
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Could not show overlay", ex);
            }
        }
    }

    /**
     * @since 0.2.1
     */
    public boolean isOverlayActive() {
        return mainStack.getChildren().size() > 1;
    }

    /**
     * @since 0.2.1
     */
    public void hideOverlay() {
        if (isOverlayActive()) {
            ObservableList<Node> children = mainStack.getChildren();
            Platform.runLater(() -> children.remove(1, children.size()));
        }
    }
}
