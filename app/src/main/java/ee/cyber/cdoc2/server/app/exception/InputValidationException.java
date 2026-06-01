package ee.cyber.cdoc2.server.app.exception;

public class InputValidationException extends Cdoc2AuthValidationException {
    private static final String ERROR_CODE = "INPUT_VALIDATION";

    public InputValidationException(String message, Throwable exception) {
        super(ERROR_CODE, message, exception);
    }
}
