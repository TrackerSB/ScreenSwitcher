package bayern.steinbrecher.screenSwitcher;

public class ScreenCreationException extends Exception {
    public ScreenCreationException() {
    }

    public ScreenCreationException(String message) {
        super(message);
    }

    public ScreenCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScreenCreationException(Throwable cause) {
        super(cause);
    }
}
