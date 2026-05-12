package ee.cyber.cdoc2.server.adapter.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ee.cyber.cdoc2.server.adapter.conf.AuthProcessCleanupConf;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthProcessCleanupJob {
    private final AuthProcessCleanupConf authProcessCleanupConf;
    private final AuthProcessRepository authProcessRepository;
    private final Clock clock;

    @Scheduled(fixedRateString = "${app.cleanup.rate}")
    public void deleteExpiredAuthProcesses() {
        int maxAge = authProcessCleanupConf.getMaxAge();
        Instant createdAtCutoff = clock.instant().minus(
            maxAge,
            ChronoUnit.MINUTES
        );

        int deletedRecords = authProcessRepository.authProcessCleanup(
            createdAtCutoff,
            authProcessCleanupConf.getDeletionLimit()
        );

        log.info("Auth process cleanup deleted {} records", deletedRecords);
    }
}
