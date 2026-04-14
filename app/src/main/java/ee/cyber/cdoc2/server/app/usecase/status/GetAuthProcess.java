package ee.cyber.cdoc2.server.app.usecase.status;

import java.util.UUID;

import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus;

public interface GetAuthProcess {
    Response execute(UUID uuid);

    record Response(
        AuthProcessStatus status,
        String endResult,
        String midSidSessionUuid
    ) {
    }
}
