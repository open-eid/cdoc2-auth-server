package ee.cyber.cdoc2.server.adapter.exception;

public class AuthProcessNotFoundException extends ClientNotFoundException {
    private static final String AUTH_PROCESS_NOT_FOUND_CODE = "AUTH_PROCESS_NOT_FOUND";

    public AuthProcessNotFoundException() {
        super(AUTH_PROCESS_NOT_FOUND_CODE, null);
    }
}
