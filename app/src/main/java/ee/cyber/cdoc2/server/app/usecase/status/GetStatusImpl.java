package ee.cyber.cdoc2.server.app.usecase.status;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;
import ee.cyber.cdoc2.server.app.usecase.status.sid.SidSession;

import static ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus.*;

@NullMarked
@Component
@RequiredArgsConstructor
public class GetStatusImpl implements GetStatus {
    private final GetAuthProcess getAuthProcess;
    private final FailAuthProcess failAuthProcess;
    private final CompleteAuthProcess completeAuthProcess;
    private final GetSidSession getSidSession;
    private final GetUnsignedJwt getUnsignedJwt;

    @Override
    public Response execute(String uuidStr) {
        UUID authProcessUuid = UUID.fromString(uuidStr);

        GetAuthProcess.Response authProcess = getAuthProcess.execute(authProcessUuid);

        if (FAILED == authProcess.status()) {
            return new Response(FAILED.name(), authProcess.endResult());
        }

        //TODO
        // if we want repeat calls to /auth/status/{authProcessUuid} for an already COMPLETED auth
        // process to return signature data, we need to store signature, certificate and
        // signature params in db.
        if (COMPLETE == authProcess.status()) {
            return new Response(COMPLETE.name(), authProcess.endResult());
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
                String unsignedSdJwtString =
                    getUnsignedJwt.execute(new GetUnsignedJwt.Request(authProcessUuid));

                GetSidSession.Signature signature = sidSession.response().signature();
                if (signature == null) {
                    throw new RuntimeException("Signature missing after authentication session "
                        + "completed successfully");
                }

                String signedSdJwt = SignSdJwt.execute(unsignedSdJwtString, signature.value());

                completeAuthProcess.execute(new CompleteAuthProcess.Request(
                    authProcessUuid,
                    sidSession.response().endResult()
                ));

                return new Response(
                    COMPLETE.name(),
                    sidSession.response().endResult(),
                    signedSdJwt,
                    sidSession.response().cert().value(),
                    new SignatureParameters(
                        signature.value(),
                        signature.serverRandom(),
                        signature.userChallenge(),
                        signature.signatureAlgorithm(),
                        new SignatureAlgorithmParameters(
                            signature.signatureAlgorithmParameters().hashAlgorithm(),
                            signature.signatureAlgorithmParameters().saltLength(),
                            signature.signatureAlgorithmParameters().trailerField()
                        ),
                        signature.flowType(),
                        authProcess.interactionsDigest(),
                        sidSession.response().interactionTypeUsed()
                    )
                );
            }
        }

        throw new RuntimeException("SID session in unknown state");
    }
}

