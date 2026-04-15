package ee.cyber.cdoc2.server.adapter.conf;

import java.net.URI;
import java.util.List;

import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceJpaRepository;
import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceUriEntity;
import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;

public class SessionNonceUriConfImpl extends SessionNonceUriConf {
    private final ServerSessionNonceJpaRepository serverSessionNonceJpaRepository;

    public SessionNonceUriConfImpl(
        ServerSessionNonceJpaRepository serverSessionNonceJpaRepository
    ) {
        this.serverSessionNonceJpaRepository = serverSessionNonceJpaRepository;
        this.uris = this.load();
    }

    @Override
    public void reload() {
        this.uris = load();
    }

    private List<URI> load() {
        List<ServerSessionNonceUriEntity> entities = serverSessionNonceJpaRepository.findAll();

        return entities.stream().map(entity -> URI.create(entity.getUri()))
            .toList();
    }
}
