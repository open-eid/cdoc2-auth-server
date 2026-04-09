package ee.cyber.cdoc2.server.app.usecase;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import static ee.cyber.cdoc2.server.app.usecase.AuthProcessStatus.FAILED;
import static ee.cyber.cdoc2.server.app.usecase.AuthProcessStatus.STARTED;

@RequiredArgsConstructor
@Component
public class GetStatusImpl implements GetStatus {
    private final GetAuthProcess getAuthProcess;
    private final FailAuthProcess failAuthProcess;
    private final SidSession sidSession;

    @Override
    public String execute(String uuidStr) {
        UUID authProcessUuid = UUID.fromString(uuidStr);

        GetAuthProcess.Response authProcess = getAuthProcess.execute(authProcessUuid);

        if (FAILED == authProcess.status()) {
            return FAILED.name();
        }

        if (STARTED == authProcess.status()) {
            if (authProcess.midSidSessionUuid() == null) {
                throw new RuntimeException("midSidSessionUuId missing on STARTED auth process");
            }

            SidSession.Response sidSessionResponse = sidSession.execute(
                UUID.fromString(authProcess.midSidSessionUuid())
            );

            if (sidSessionResponse.isCompletedNotOk()) {
                failAuthProcess.execute(new FailAuthProcess.Request(authProcessUuid));
                return FAILED.name();
            }

            if (sidSessionResponse.isCompletedOk()) {

            }

            return sidSessionResponse.state();
        }

        return authProcess.status().name();
    }
}
