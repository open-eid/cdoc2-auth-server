package ee.cyber.cdoc2.server.app.usecase.status;

import org.jspecify.annotations.Nullable;

import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus;

public interface GetStatus {
    Response execute(String uuid);

    record Response(
        AuthProcessStatus status,
        @Nullable String endResult,
        @Nullable String sessionToken,
        @Nullable String signingCertificate
    ) {
        Response(AuthProcessStatus status) {
            this(status, null, null, null);
        }

        Response(AuthProcessStatus status, @Nullable String endResult) {
            this(status, endResult, null, null);
        }
    }
}
