package ee.cyber.cdoc2.server.adapter.conf;

import java.net.URI;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;

@Configuration
public class SessionNonceUriConfImpl implements SessionNonceUriConf {
    private final List<URI> uris;

    @ConfigurationProperties(prefix = "app.session-nonce")
    public record AppProperties(List<String> uris) {
    }

    public SessionNonceUriConfImpl(
        AppProperties props
    ) {
        this.uris = props.uris.stream()
            .map(URI::create)
            .toList();
    }

    @Override
    public List<URI> getUris() {
        return uris;
    }
}
