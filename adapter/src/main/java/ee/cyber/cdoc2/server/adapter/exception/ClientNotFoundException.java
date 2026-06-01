package ee.cyber.cdoc2.server.adapter.exception;

public class ClientNotFoundException extends ClientException {
    public ClientNotFoundException(String code, String message) {
        super(code, message);
    }
}
