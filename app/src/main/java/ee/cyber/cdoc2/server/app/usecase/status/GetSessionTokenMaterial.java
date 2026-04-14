package ee.cyber.cdoc2.server.app.usecase.status;

import java.util.UUID;

public interface GetSessionTokenMaterial {
    Response execute(Request request);

    record Request(UUID uuid) {
    }

    record Response(
        String unsignedJwt,
        String interactionsDigest,
        String rpChallenge
    ) {
    }
}
