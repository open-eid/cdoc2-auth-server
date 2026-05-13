package ee.cyber.cdoc2.server.app.usecase.status.sid;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.usecase.status.GetSessionTokenMaterial;

@Component
@RequiredArgsConstructor
public class CreateSidSessionToken {
    private final CreateSignedSdJwtForSid createSignedSdJwtForSid;
    private final GetSessionTokenMaterial getSessionTokenMaterial;

    public String execute(UUID authProcessUuid, GetSidSession.@Nullable Response sessionResponse) {
        GetSidSession.Response sidSessionResponse = validateSessionResponse(sessionResponse);

        GetSidSession.Signature signature = getSidSignature(
            sidSessionResponse
        );

        GetSessionTokenMaterial.Response sessionTokenMaterial =
            getSessionTokenMaterial.execute(
                new GetSessionTokenMaterial.Request(authProcessUuid)
            );

        return createSignedSdJwtForSid.execute(
            sessionTokenMaterial.unsignedJwt(),
            new CreateSignedSdJwtForSid.SidSignatureParams(
                signature,
                sessionTokenMaterial.rpChallenge(),
                sessionTokenMaterial.interactionsDigest(),
                sidSessionResponse.interactionTypeUsed()
            ));
    }

    private GetSidSession.Signature getSidSignature(GetSidSession.Response response) {
        if (response.signature() == null) {
            throw new IllegalStateException("Signature missing in SID session response");
        }

        return response.signature();
    }

    private GetSidSession.Response validateSessionResponse(
        GetSidSession.@Nullable Response sessionResponse
    ) {
        if (sessionResponse == null) {
            throw new IllegalStateException("SID session response missing");
        }

        return sessionResponse;
    }
}
