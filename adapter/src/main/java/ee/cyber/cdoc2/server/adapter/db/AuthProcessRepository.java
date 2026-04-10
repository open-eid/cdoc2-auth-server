package ee.cyber.cdoc2.server.adapter.db;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import ee.cyber.cdoc2.server.adapter.conf.SessionNonceUriDbCache;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessJpaRepository;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessSessionNonceEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceUriEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.projection.AuthProcessStatusMidSidSession;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus;
import ee.cyber.cdoc2.server.app.usecase.startauth.StoreAuthProcess;
import ee.cyber.cdoc2.server.app.usecase.status.FailAuthProcess;
import ee.cyber.cdoc2.server.app.usecase.status.GetAuthProcess;
import ee.cyber.cdoc2.server.app.usecase.status.GetUnsignedJwt;

@NullMarked
@Repository
@RequiredArgsConstructor
public class AuthProcessRepository implements StoreAuthProcess, GetAuthProcess, FailAuthProcess,
    GetUnsignedJwt {
    private final AuthProcessJpaRepository authProcessJpaRepository;
    private final SessionNonceUriDbCache sessionNonceUriDbCache;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void execute(StoreAuthProcess.Request request) {
        AuthProcessEntity authProcessEntity = new AuthProcessEntity();

        List<AuthProcessSessionNonceEntity> nonceEntities = request.sessionNonces().stream()
            .map(uriSessionNonce -> {
                    AuthProcessSessionNonceEntity nonceEntity =
                        new AuthProcessSessionNonceEntity();
                    nonceEntity.setAuthProcess(authProcessEntity);
                    nonceEntity.setSessionNonce(uriSessionNonce.nonce());

                    nonceEntity.setServerUri(createNonceUriEntityReference(
                        uriSessionNonce.uri()
                    ));

                    return nonceEntity;
                }
            ).toList();

        authProcessEntity.setServerSessionNonce(nonceEntities);
        authProcessEntity.setUuid(request.authUuid().toString());
        authProcessEntity.setMidSidSessionId(request.midSidSessionId().toString());
        authProcessEntity.setUnsignedSdJwt(request.unsignedSdJwt());
        authProcessEntity.setStatus(AuthProcessStatus.STARTED.name());

        authProcessJpaRepository.save(authProcessEntity);
    }

    @Override
    public GetAuthProcess.Response execute(UUID uuid) {
        AuthProcessStatusMidSidSession projection =
            authProcessJpaRepository.findStatusMidSidSessionByUuid(uuid.toString());
        return new Response(
            AuthProcessStatus.valueOf(projection.getStatus()),
            projection.getMidSidSessionId()
        );
    }

    @Override
    public void execute(FailAuthProcess.Request request) {
        int updated = authProcessJpaRepository.updateStatus(
            request.uuid().toString(),
            AuthProcessStatus.FAILED.name()
        );

        if (updated != 1) {
            throw new RuntimeException("Auth process state update unsuccessful");
        }
    }

    private ServerSessionNonceUriEntity createNonceUriEntityReference(URI uri) {
        return entityManager.getReference(
            ServerSessionNonceUriEntity.class,
            sessionNonceUriDbCache.sessionNonceUriEntityIdByUri(uri)
        );
    }

    @Override
    public String execute(GetUnsignedJwt.Request request) {
        return authProcessJpaRepository.findUnsignedJwtByUuid(request.uuid().toString())
            .getUnsignedSdJwt();
    }
}
