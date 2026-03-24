package ee.cyber.cdoc2.server.adapter.db;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.AuthProcessJpaRepository;
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
        AuthProcessEntity entity = new AuthProcessEntity();
        entity.setUuid(request.authUuid().toString());
        entity.setMidSidSessionId(request.midSidSessionId());
        entity.setStatus(request.authStatus().name());
        authProcessJpaRepository.save(entity);
    }

    @Override
    public String execute(UUID uuid) {
        return authProcessJpaRepository.findByUuid(uuid.toString()).getStatus();
    }
}
