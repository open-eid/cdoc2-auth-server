package ee.cyber.cdoc2.server.app.usecase.status;

import java.util.UUID;

public interface FailAuthProcess {
    void execute(Request request);

    record Request(UUID uuid, String endResult) {
    }
}
