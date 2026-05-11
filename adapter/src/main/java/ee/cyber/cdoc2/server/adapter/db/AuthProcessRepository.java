package ee.cyber.cdoc2.server.adapter.db;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessJpaRepository;
import ee.cyber.cdoc2.server.adapter.db.jpa.projection.AuthProcessSessionTokenMaterial;
import ee.cyber.cdoc2.server.adapter.db.jpa.projection.AuthProcessStatusMidSidSession;
import ee.cyber.cdoc2.server.adapter.exception.AuthProcessNotFoundException;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus;
import ee.cyber.cdoc2.server.app.usecase.startauth.StoreAuthProcess;
import ee.cyber.cdoc2.server.app.usecase.status.CompleteAuthProcess;
import ee.cyber.cdoc2.server.app.usecase.status.FailAuthProcess;
import ee.cyber.cdoc2.server.app.usecase.status.GetAuthProcess;
import ee.cyber.cdoc2.server.app.usecase.status.GetSessionTokenMaterial;

@NullMarked
@Repository
@RequiredArgsConstructor
public class AuthProcessRepository implements StoreAuthProcess, GetAuthProcess, FailAuthProcess,
    CompleteAuthProcess, GetSessionTokenMaterial {
    private final AuthProcessJpaRepository authProcessJpaRepository;

    @Override
    @Transactional
    public void execute(StoreAuthProcess.Request request) {
        AuthProcessEntity authProcessEntity = new AuthProcessEntity();

        authProcessEntity.setUuid(request.authUuid().toString());
        authProcessEntity.setMidSidSessionId(request.midSidSessionId().toString());
        authProcessEntity.setInteractionsDigest(request.interactionsDigest());
        authProcessEntity.setRpChallenge(request.rpChallenge());
        authProcessEntity.setUnsignedSdJwt(request.unsignedSdJwt());
        authProcessEntity.setStatus(AuthProcessStatus.STARTED.name());

        authProcessJpaRepository.save(authProcessEntity);
    }

    @Override
    public GetAuthProcess.Response execute(UUID uuid) {
        AuthProcessStatusMidSidSession projection =
            authProcessJpaRepository.findStatusMidSidSessionByUuid(uuid.toString());

        if (projection == null) {
            throw new AuthProcessNotFoundException();
        }

        return new GetAuthProcess.Response(
            AuthProcessStatus.valueOf(projection.getStatus()),
            projection.getEndResult(),
            projection.getMidSidSessionId(),
            projection.getSessionToken(),
            projection.getSigningCert()
        );
    }

    @Override
    public void execute(FailAuthProcess.Request request) {
        int updated = authProcessJpaRepository.updateStatus(
            request.uuid().toString(),
            AuthProcessStatus.FAILED.name(),
            request.endResult()
        );

        if (updated != 1) {
            throw new RuntimeException("Auth process state update unsuccessful");
        }
    }

    @Override
    public void execute(CompleteAuthProcess.Request request) {
        int updated = authProcessJpaRepository.updateStatus(
            request.uuid().toString(),
            AuthProcessStatus.COMPLETE.name(),
            request.endResult(),
            request.sessionToken(),
            request.signingCert()
        );

        if (updated != 1) {
            throw new RuntimeException("Auth process state update unsuccessful");
        }
    }

    @Override
    public GetSessionTokenMaterial.Response execute(GetSessionTokenMaterial.Request request) {
        AuthProcessSessionTokenMaterial projection = authProcessJpaRepository
            .findSessionTokenMaterialByUuid(request.uuid().toString());

        return new GetSessionTokenMaterial.Response(
            projection.getUnsignedSdJwt(),
            projection.getInteractionsDigest(),
            projection.getRpChallenge()
        );
    }

    public int authProcessCleanup(Instant createdAtCutoff) {
        return authProcessJpaRepository.deleteExpiredAuthProcesses(createdAtCutoff);
    }
}
