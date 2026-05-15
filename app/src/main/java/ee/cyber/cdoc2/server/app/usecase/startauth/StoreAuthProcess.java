package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;

public interface StoreAuthProcess {

    void execute(Request request);

    record Request(
        UUID authUuid,
        AuthProcessType type,
        UUID midSidSessionId,
        String unsignedSdJwt,
        @Nullable String interactionsDigest,
        @Nullable String rpChallenge
    ) {
    }
}
