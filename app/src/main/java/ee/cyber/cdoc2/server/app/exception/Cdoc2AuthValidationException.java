package ee.cyber.cdoc2.server.app.exception;

public class Cdoc2AuthValidationException extends RuntimeException {
    private final String code;

    public Cdoc2AuthValidationException(String code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
