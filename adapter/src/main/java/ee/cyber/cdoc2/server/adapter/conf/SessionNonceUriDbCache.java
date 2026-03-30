package ee.cyber.cdoc2.server.adapter.conf;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceJpaRepository;
import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceUriEntity;

@Component
@RequiredArgsConstructor
public class SessionNonceUriDbCache {
    private final ServerSessionNonceJpaRepository serverSessionNonceJpaRepository;

    private Map<URI, Long> dbUriIdCache;

    public Long sessionNonceUriEntityIdByUri(URI uri) {
        return dbUriIdCache.get(uri);
    }

    List<URI> load() {
        List<ServerSessionNonceUriEntity> entities = serverSessionNonceJpaRepository.findAll();

        this.dbUriIdCache = entities.stream().collect(Collectors.toMap(
            entity -> URI.create(entity.getUri()),
            ServerSessionNonceUriEntity::getId
        ));

        return this.dbUriIdCache.keySet()
            .stream().toList();
    }
}
