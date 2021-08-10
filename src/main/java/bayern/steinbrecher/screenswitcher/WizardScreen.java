package bayern.steinbrecher.screenswitcher;

import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardPage;
import bayern.steinbrecher.wizard.WizardState;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public abstract class WizardScreen<R> extends Screen<WizardScreenController> {
    private final Wizard wizard;

    protected WizardScreen() throws LoadException {
        super(WizardScreen.class.getResource("WizardScreen.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.screens.WizardScreen"));
        this.wizard = Wizard.create(generatePages());
    }

    @Override
    protected void afterControllerIsInitialized(@NotNull WizardScreenController controller) {
        super.afterControllerIsInitialized(controller);
        wizard.stateProperty()
                .addListener(((obs, oldVal, newVal) -> {
                    if (newVal == WizardState.FINISHED) {
                        controller.getScreenManager()
                                .switchBack();
                    }
                }));
        wizard.atFinishProperty()
                .addListener((observable, oldValue, newValue) -> {
                    System.out.println("HERE"); // FIXME Remove debug print
                });
    }

    @Override
    @NotNull
    public final Parent create(@NotNull ScreenManager manager) {
        return wizard.getRoot();
    }

    @NotNull
    public final Optional<R> getResult() {
        return wizard.getVisitedPages()
                .map(this::getResultImpl);
    }

    @NotNull
    protected abstract Map<String, WizardPage<?, ?>> generatePages() throws LoadException;

    @Nullable
    protected abstract R getResultImpl(@NotNull Collection<String> visitedPages);
}
