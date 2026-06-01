package ee.cyber.cdoc2.server.adapter.exception;

public class ClientException extends RuntimeException {
    private final String code;

    public ClientException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
