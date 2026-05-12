package ee.cyber.cdoc2.server.app.exception;

public class InputValidationException extends RuntimeException {
    public InputValidationException(String message, Throwable exception) {
        super(message, exception);
    }
}
