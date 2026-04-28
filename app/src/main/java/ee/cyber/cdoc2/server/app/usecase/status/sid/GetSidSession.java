package ee.cyber.cdoc2.server.app.usecase.status.sid;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

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
        String flowType,
        SignatureAlgorithmParameters signatureAlgorithmParameters
    ) {
    }

    record SignatureAlgorithmParameters(
        String hashAlgorithm,
        MaskGenAlgorithm maskGenAlgorithm,
        Integer saltLength,
        String trailerField
    ) {
    }

    record MaskGenAlgorithm(
        String algorithm,
        Parameters parameters
    ) {

        public record Parameters(
            String hashAlgorithm
        ) {
        }
    }

    record Certificate(String value, String certificateLevel) {
    }
}
