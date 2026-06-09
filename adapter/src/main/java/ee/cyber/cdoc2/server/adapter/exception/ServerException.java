package ee.cyber.cdoc2.server.adapter.exception;

public class ServerException extends RuntimeException {
    private final String code;

    public ServerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
