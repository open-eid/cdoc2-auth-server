package ee.cyber.cdoc2.server.adapter.exception;

public class ClientBadRequestException extends ClientException {
    public ClientBadRequestException(String code, String message) {
        super(code, message);
    }
}
