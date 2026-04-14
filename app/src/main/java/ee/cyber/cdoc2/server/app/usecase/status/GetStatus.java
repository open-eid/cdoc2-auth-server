package ee.cyber.cdoc2.server.app.usecase.status;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface GetStatus {
    Response execute(String uuid);

    record Response(
        String status,
        @Nullable String endResult,
        @Nullable String sessionToken,
        @Nullable String signingCertificate,
        @Nullable SignatureParameters signatureParameters
    ) {
        Response(String status) {
            this(status, null, null, null, null);
        }

        Response(String status, @Nullable String endResult) {
            this(status, endResult, null, null, null);
        }
    }

    record SignatureParameters(
        String value,
        String serverRandom,
        String userChallenge,
        String signatureAlgorithm,
        SignatureAlgorithmParameters signatureAlgorithmParameters,
        String flowType,
        String interactionsDigest,
        String interactionTypeUsed
    ) {
    }

    record SignatureAlgorithmParameters(
        String hashAlgorithm,
        Integer saltLength,
        String trailerField
    ) {
    }
}
