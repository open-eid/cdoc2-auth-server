package ee.cyber.cdoc2.server.app.usecase.status.sid;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.usecase.status.GetSessionTokenMaterial;
import ee.cyber.cdoc2.server.app.usecase.status.SessionStatusHolder;

@Component
@RequiredArgsConstructor
public class CreateSidSessionToken {
    private final CreateSignedSdJwtForSid createSignedSdJwtForSid;
    private final GetSessionTokenMaterial getSessionTokenMaterial;

    public String execute(UUID authProcessUuid, SessionStatusHolder sessionStatusHolder) {
        GetSessionTokenMaterial.Response sessionTokenMaterial =
            getSessionTokenMaterial.execute(
                new GetSessionTokenMaterial.Request(authProcessUuid)
            );

        GetSidSession.Response sidSessionResponse = getSidSessionResponse(sessionStatusHolder);

        GetSidSession.Signature signature = getSidSignature(
            sidSessionResponse
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
            throw new RuntimeException("Signature missing in SID session response");
        }

        return response.signature();
    }

    private GetSidSession.Response getSidSessionResponse(SessionStatusHolder sessionStatusHolder) {
        if (sessionStatusHolder.getSidSessionResponse() == null) {
            throw new RuntimeException("Sid session response missing");
        }

        return sessionStatusHolder.getSidSessionResponse();
    }
}
