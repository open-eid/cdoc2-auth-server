package ee.cyber.cdoc2.server.app.usecase.status.sid;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface GetSidSession {
    Response execute(UUID sessionId);

    record Response(
        String state,
        @Nullable String endResult,
        @Nullable Signature signature,
        @Nullable Certificate cert,
        @Nullable String interactionTypeUsed
    ) {
    }

    record Signature(
        String value,
        String serverRandom,
        String userChallenge,
        String signatureAlgorithm,
        SignatureAlgorithmParameters signatureAlgorithmParameters
    ) {
    }

    record SignatureAlgorithmParameters(
        String hashAlgorithm,
        Integer saltLength,
        String trailerField
    ) {
    }

    record Certificate(String value, String certificateLevel) {
    }
}
