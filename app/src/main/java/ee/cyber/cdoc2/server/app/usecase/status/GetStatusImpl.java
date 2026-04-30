package ee.cyber.cdoc2.server.app.usecase.status;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;
import ee.cyber.cdoc2.server.app.usecase.status.sid.SidSession;

import static ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus.*;

@Component
@RequiredArgsConstructor
public class GetStatusImpl implements GetStatus {
    private final GetAuthProcess getAuthProcess;
    private final FailAuthProcess failAuthProcess;
    private final CompleteAuthProcess completeAuthProcess;
    private final GetSidSession getSidSession;
    private final GetSessionTokenMaterial getSessionTokenMaterial;
    private final CreateSignedSdJwtWithSidSignature createSignedSdJwtWithSidSignature;

    @Override
    public Response execute(String uuidStr) {
        UUID authProcessUuid = UUID.fromString(uuidStr);

        GetAuthProcess.Response authProcess = getAuthProcess.execute(authProcessUuid);

        if (FAILED == authProcess.status()) {
            return new Response(FAILED.name(), authProcess.endResult());
        }

        if (COMPLETE == authProcess.status()) {
            return new Response(
                COMPLETE.name(),
                authProcess.endResult(),
                authProcess.sessionToken(),
                authProcess.signingCert()
            );
        }

        if (STARTED == authProcess.status()) {
            if (authProcess.midSidSessionUuid() == null) {
                throw new RuntimeException("midSidSessionUuId missing on STARTED auth process");
            }
            SidSession sidSession = new SidSession(this.getSidSession.execute(
                UUID.fromString(authProcess.midSidSessionUuid())
            ));

            if (sidSession.isRunning()) {
                return new Response(STARTED.name());
            }

            if (sidSession.isCompletedNotOk()) {
                failAuthProcess.execute(new FailAuthProcess.Request(
                    authProcessUuid,
                    sidSession.response().endResult()
                ));
                return new Response(FAILED.name(), sidSession.response().endResult());
            }

            if (sidSession.isCompletedOk()) {
                GetSessionTokenMaterial.Response sessionTokenMaterial =
                    getSessionTokenMaterial.execute(
                        new GetSessionTokenMaterial.Request(authProcessUuid)
                    );

                GetSidSession.Signature signature = getSidSignature(
                    sidSession.response()
                );
                GetSidSession.Certificate signingCertificate = getSigningCertificate(
                    sidSession.response()
                );

                String signedSdJwt = createSignedSdJwtWithSidSignature.execute(
                    sessionTokenMaterial.unsignedJwt(),
                    new CreateSignedSdJwtWithSidSignature.SidSignatureParams(
                        signature,
                        sessionTokenMaterial.rpChallenge(),
                        sessionTokenMaterial.interactionsDigest(),
                        sidSession.response().interactionTypeUsed()
                    ));

                completeAuthProcess.execute(new CompleteAuthProcess.Request(
                    authProcessUuid,
                    sidSession.response().endResult(),
                    signedSdJwt,
                    signingCertificate.value()
                ));

                return new Response(
                    COMPLETE.name(),
                    sidSession.response().endResult(),
                    signedSdJwt,
                    signingCertificate.value()
                );
            }
        }

        throw new RuntimeException("SID session in unknown state");
    }

    private GetSidSession.Certificate getSigningCertificate(GetSidSession.Response response) {
        if (response.cert() == null) {
            throw new RuntimeException("Certificate missing in SID session response");
        }

        return response.cert();
    }

    private GetSidSession.Signature getSidSignature(GetSidSession.Response response) {
        if (response.signature() == null) {
            throw new RuntimeException("Signature missing in SID session response");
        }

        return response.signature();
    }
}

