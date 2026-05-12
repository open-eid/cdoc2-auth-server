package ee.cyber.cdoc2.server.app.usecase.status;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;

public interface GetAuthProcess {
    Response execute(UUID uuid);

    record Response(
        AuthProcessStatus status,
        AuthProcessType type,
        @Nullable String endResult,
        @Nullable String midSidSessionUuid,
        @Nullable String sessionToken,
        @Nullable String signingCert
    ) {
    }
}
