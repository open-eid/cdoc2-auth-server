package ee.cyber.cdoc2.server.adapter.db;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import ee.cyber.cdoc2.TestApplication;
import ee.cyber.cdoc2.server.adapter.conf.AuthProcessCleanupConf;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessJpaRepository;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest()
@ContextConfiguration(classes = TestApplication.class)
class AuthProcessCleanupJobTest {
    private static final int AUTH_PROCESS_MAX_AGE_MINUTES = 5;
    private static final int AUTH_PROCESS_DELETION_LIMIT = 3;
    private static final Instant INSTANT_NOW = Instant.parse("2026-05-15T12:30:00Z");
    private static final Clock CLOCK_FIXED_NOW = Clock.fixed(INSTANT_NOW, ZoneId.systemDefault());

    @Autowired
    private AuthProcessJpaRepository authProcessJpaRepository;
    @Autowired
    private TestEntityManager entityManager;

    private AuthProcessCleanupJob authProcessCleanupJob;

    @BeforeEach
    void setUp() {
        AuthProcessRepository authProcessRepository = new AuthProcessRepository(
            authProcessJpaRepository
        );

        authProcessCleanupJob = new AuthProcessCleanupJob(
            new AuthProcessCleanupConf(
                new AuthProcessCleanupConf.AppProperties(
                    AUTH_PROCESS_MAX_AGE_MINUTES,
                    AUTH_PROCESS_DELETION_LIMIT
                )
            ),
            authProcessRepository,
            CLOCK_FIXED_NOW
        );

        authProcessJpaRepository.deleteAll();
    }

    @Test
    void cleanupShouldNotDeleteFreshAuthProcess() {
        storeAuthProcessEntity(INSTANT_NOW);

        authProcessCleanupJob.deleteExpiredAuthProcesses();

        assertEquals(1, authProcessJpaRepository.findAll().size());
    }

    @Test
    void cleanupShouldDeleteFreshAuthProcessOlderThanMaxAge() {
        storeAuthProcessEntity(
            INSTANT_NOW.minus(
                AUTH_PROCESS_MAX_AGE_MINUTES + 1,
                ChronoUnit.MINUTES
            )
        );

        authProcessCleanupJob.deleteExpiredAuthProcesses();

        assertEquals(0, authProcessJpaRepository.findAll().size());
    }

    @Test
    void cleanupRecordDeletionCountShouldBeLimitedByConfiguredValue() {
        for (int entityNumber = 0; entityNumber < AUTH_PROCESS_DELETION_LIMIT + 1; entityNumber++) {
            storeAuthProcessEntity(
                INSTANT_NOW.minus(
                    AUTH_PROCESS_MAX_AGE_MINUTES + 1,
                    ChronoUnit.MINUTES
                )
            );
        }
        authProcessCleanupJob.deleteExpiredAuthProcesses();

        assertEquals(1, authProcessJpaRepository.findAll().size());
    }

    private void storeAuthProcessEntity(Instant createdAt) {
        AuthProcessEntity entity = new AuthProcessEntity();

        entity.setUuid(UUID.randomUUID().toString());
        entity.setType(AuthProcessType.SID.name());
        entity.setMidSidSessionId("midSidSessionId");
        entity.setInteractionsDigest("interactionsDigest");
        entity.setRpChallenge("rpChallenge");
        entity.setUnsignedSdJwt("unsignedSdJWT");
        entity.setStatus(AuthProcessStatus.STARTED.name());

        entityManager.persist(entity);

        entityManager.getEntityManager()
            .createQuery("UPDATE AuthProcessEntity a SET a.createdAt = :ts WHERE a.id = :id")
            .setParameter("ts", createdAt)
            .setParameter("id", entity.getId())
            .executeUpdate();

        entityManager.flush();
        entityManager.clear();
    }
}
