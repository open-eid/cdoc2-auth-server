package ee.cyber.cdoc2.server.adapter.conf;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceJpaRepository;
import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceUriEntity;

@Component
@RequiredArgsConstructor
public class SessionNonceUriDb {
    private final ServerSessionNonceJpaRepository serverSessionNonceJpaRepository;

    List<URI> load() {
        List<ServerSessionNonceUriEntity> entities = serverSessionNonceJpaRepository.findAll();

        return entities.stream().map(entity -> URI.create(entity.getUri()))
            .toList();
    }
}
