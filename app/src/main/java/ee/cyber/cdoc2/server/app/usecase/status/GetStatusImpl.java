package ee.cyber.cdoc2.server.app.usecase.status;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;
import ee.cyber.cdoc2.server.app.usecase.status.mid.CreateMidSessionToken;
import ee.cyber.cdoc2.server.app.usecase.status.mid.GetMidSession;
import ee.cyber.cdoc2.server.app.usecase.status.sid.CreateSidSessionToken;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

import static ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus.*;

@Component
@RequiredArgsConstructor
public class GetStatusImpl implements GetStatus {
    private final GetAuthProcess getAuthProcess;
    private final FailAuthProcess failAuthProcess;
    private final CompleteAuthProcess completeAuthProcess;
    private final GetSidSession getSidSession;
    private final GetMidSession getMidSession;
    private final CreateMidSessionToken createMidSessionToken;
    private final CreateSidSessionToken createSidSessionToken;

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

        AuthProcessType authProcessType = authProcess.type();

        if (STARTED == authProcess.status()) {
            if (authProcess.midSidSessionUuid() == null) {
                throw new RuntimeException("midSidSessionUuId missing on STARTED auth process");
            }

            SessionStatusHolder sidMidSessionStatus = switch (authProcessType) {
                case AuthProcessType.SID -> new SessionStatusHolder(this.getSidSession.execute(
                    UUID.fromString(authProcess.midSidSessionUuid())
                ));
                case AuthProcessType.MID -> new SessionStatusHolder(this.getMidSession.execute(
                    UUID.fromString(authProcess.midSidSessionUuid())
                ));
            };

            if (sidMidSessionStatus.isRunning()) {
                return new Response(STARTED.name());
            }

            if (sidMidSessionStatus.isCompletedNotOk()) {
                failAuthProcess.execute(new FailAuthProcess.Request(
                    authProcessUuid,
                    sidMidSessionStatus.getEndResult()
                ));
                return new Response(FAILED.name(), sidMidSessionStatus.getEndResult());
            }

            if (sidMidSessionStatus.isCompletedOk()) {
                String signedSdJwt = switch (authProcessType) {
                    case AuthProcessType.SID -> createSidSessionToken
                        .execute(authProcessUuid, sidMidSessionStatus);
                    case AuthProcessType.MID -> createMidSessionToken.execute(authProcessUuid);
                };

                completeAuthProcess.execute(new CompleteAuthProcess.Request(
                    authProcessUuid,
                    sidMidSessionStatus.getEndResult(),
                    signedSdJwt,
                    sidMidSessionStatus.getCert()
                ));

                return new Response(
                    COMPLETE.name(),
                    sidMidSessionStatus.getEndResult(),
                    signedSdJwt,
                    sidMidSessionStatus.getCert()
                );
            }
        }

        throw new RuntimeException(authProcessType + " session in unknown state");
    }
}

