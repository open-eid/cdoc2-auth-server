package ee.cyber.cdoc2.server.app.usecase;

import java.util.UUID;

public interface GetAuthProcess {
    Response execute(UUID uuid);

    record Response(
        AuthProcessStatus status,
        String midSidSessionUuid
    ) {
    }
}
