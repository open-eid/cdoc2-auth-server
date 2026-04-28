package ee.cyber.cdoc2.server.app.usecase.status;

import org.jspecify.annotations.Nullable;

public interface GetStatus {
    Response execute(String uuid);

    record Response(
        String status,
        @Nullable String endResult,
        @Nullable String sessionToken,
        @Nullable String signingCertificate
    ) {
        Response(String status) {
            this(status, null, null, null);
        }

        Response(String status, @Nullable String endResult) {
            this(status, endResult, null, null);
        }
    }
}
