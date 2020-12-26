package bayern.steinbrecher.screenSwitcher;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenManager {
    private static final Logger LOGGER = Logger.getLogger(ScreenManager.class.getName());
    private final StackPane screenStack = new StackPane();
    private final Map<Node, RunnableFuture<?>> waitForWizardEndedScreens = new HashMap<>();

    public ScreenManager(@NotNull Stage screenBaseStage) {
        screenBaseStage.setScene(new Scene(screenStack));
    }

    /**
     * This method runs the given task on the FXApplicationThread. If the current {@link Thread} is not the
     * FXApplicationThread this method calls {@link Platform#runLater(Runnable)} and waits for it to finish.
     *
     * @see javafx.application.Platform#runLater(Runnable)
     */
    private static void platformRunLaterAndWait(Runnable task) throws InterruptedException {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            CountDownLatch doneLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    task.run();
                } finally {
                    doneLatch.countDown();
                }
            });
            doneLatch.await();
        }
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
            platformRunLaterAndWait(() -> screenStack.getChildren().add(content));
        } catch (InterruptedException ex) {
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
        RunnableFuture<R> callbackTask = new FutureTask<>(() -> {
            return nextScreen.getResult()
                    .orElseThrow(
                            () -> new AssertionError("The callback must only be called if the result is available"));
        });
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
}