package ee.cyber.cdoc2.server.adapter.db;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessJpaRepository;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessSessionNonceEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceUriEntity;
import ee.cyber.cdoc2.server.app.usecase.GetAuthState;
import ee.cyber.cdoc2.server.app.usecase.StoreAuth;

@NullMarked
@Repository
@RequiredArgsConstructor
public class AuthRepository implements StoreAuth, GetAuthState {
    private final AuthProcessJpaRepository authProcessJpaRepository;

    @Override
    @Transactional
    public void execute(StoreAuth.Request request) {
        ServerSessionNonceUriEntity nonceUriEntity = new ServerSessionNonceUriEntity();
        nonceUriEntity.setUri("localhost:1234");

        AuthProcessSessionNonceEntity nonceEntity = new AuthProcessSessionNonceEntity();
        nonceEntity.setSessionNonce("12345");
        nonceEntity.setServerUri(nonceUriEntity);

        AuthProcessEntity authProcessEntity = new AuthProcessEntity();
        nonceEntity.setAuthProcess(authProcessEntity);

        authProcessEntity.setServerSessionNonce(List.of(nonceEntity));
        authProcessEntity.setUuid(request.authUuid().toString());
        authProcessEntity.setMidSidSessionId(request.midSidSessionId());
        authProcessEntity.setStatus(request.authStatus().name());

        authProcessJpaRepository.save(authProcessEntity);
    }

    @Override
    public String execute(UUID uuid) {
        AuthProcessEntity entity = authProcessJpaRepository.findByUuid(uuid.toString());
        return entity.getStatus();
    }
}
