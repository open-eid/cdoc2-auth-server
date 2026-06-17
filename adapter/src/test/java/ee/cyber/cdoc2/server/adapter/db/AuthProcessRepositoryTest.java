package ee.cyber.cdoc2.server.adapter.db;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import ee.cyber.cdoc2.TestApplication;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessJpaRepository;
import ee.cyber.cdoc2.server.adapter.db.jpa.projection.AuthProcessSessionTokenMaterial;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;
import ee.cyber.cdoc2.server.app.usecase.startauth.StoreAuthProcess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ContextConfiguration(classes = TestApplication.class)
class AuthProcessRepositoryTest {

    @Autowired
    private AuthProcessJpaRepository authProcessJpaRepository;

    @Test
    void interactionsDigestIsNullForMidAuthProcess() {
        AuthProcessRepository repository = new AuthProcessRepository(authProcessJpaRepository);
        UUID authUuid = UUID.randomUUID();

        repository.execute(new StoreAuthProcess.Request(
            authUuid,
            AuthProcessType.MID,
            UUID.randomUUID(),
            "unsigned-sd-jwt",
            null,
            "rp-challenge"
        ));

        AuthProcessSessionTokenMaterial material =
            authProcessJpaRepository.findSessionTokenMaterialByUuid(authUuid.toString());

        assertNull(material.getInteractionsDigest());
        assertEquals("unsigned-sd-jwt", material.getUnsignedSdJwt());
        assertEquals("rp-challenge", material.getRpChallenge());
    }

    @Test
    void interactionsDigestIsStoredForSidAuthProcess() {
        AuthProcessRepository repository = new AuthProcessRepository(authProcessJpaRepository);
        UUID authUuid = UUID.randomUUID();
        String interactionsDigest = "interactions-digest";

        repository.execute(new StoreAuthProcess.Request(
            authUuid,
            AuthProcessType.SID,
            UUID.randomUUID(),
            "unsigned-sd-jwt",
            interactionsDigest,
            "rp-challenge"
        ));

        AuthProcessSessionTokenMaterial material =
            authProcessJpaRepository.findSessionTokenMaterialByUuid(authUuid.toString());

        assertNotNull(material.getInteractionsDigest());
        assertEquals(interactionsDigest, material.getInteractionsDigest());
        assertEquals("unsigned-sd-jwt", material.getUnsignedSdJwt());
        assertEquals("rp-challenge", material.getRpChallenge());
    }
}
