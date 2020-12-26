package bayern.steinbrecher.screens;

import bayern.steinbrecher.wizard.EmbeddedWizardPage;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardPage;
import bayern.steinbrecher.wizard.WizardState;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class WizardScreen<R> extends Screen<WizardScreenController> {
    private static final Logger LOGGER = Logger.getLogger(WizardScreen.class.getName());
    private final Wizard wizard;

    protected WizardScreen() throws LoadException {
        super(WizardScreen.class.getResource("WizardScreen.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.screens.WizardScreen"));
        Map<String, EmbeddedWizardPage<?>> embeddedWizardPages = generatePages()
                .entrySet()
                .stream()
                .map(entry -> {
                    try {
                        return Map.entry(entry.getKey(), entry.getValue().generateEmbeddableWizardPage());
                    } catch (LoadException ex) {
                        LOGGER.log(Level.SEVERE,
                                String.format("Could not generate wizard page for '%s'", entry.getKey()), ex);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        this.wizard = Wizard.create(embeddedWizardPages);
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
