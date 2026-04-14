package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StoreAuthProcess {

    void execute(Request request);

    record Request(
        UUID authUuid,
        UUID midSidSessionId,
        String unsignedSdJwt,
        String interactionsDigest,
        String rpChallenge
    ) {
    }
}
