package ee.cyber.cdoc2.server.adapter.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.adapter.resource.ResourceLoaderWrapper;

@Configuration
public class WellKnownJwkConf {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ResourceLoaderWrapper resourceLoader;

    private final WellKnownResponse jwk;

    @ConfigurationProperties(prefix = "app.well-known")
    public record AppProperties(
        @Nullable String jwk
    ) {
    }

    public WellKnownJwkConf(
        AppProperties props,
        ResourceLoaderWrapper resourceLoader
    ) throws IOException {
        validateConf(props);
        this.resourceLoader = resourceLoader;
        this.jwk = getWellKnownJwkFromResource(props.jwk);
    }

    private WellKnownResponse getWellKnownJwkFromResource(String name) throws IOException {
        Objects.requireNonNull(name);
        try (InputStream is = resourceLoader.loadResource(name).getInputStream()) {
            return OBJECT_MAPPER.readValue(is, WellKnownResponse.class);
        }
    }

    public WellKnownResponse getJwk() {
        return jwk;
    }

    private void validateConf(AppProperties props) {
        if (props.jwk == null || props.jwk.isBlank()) {
            throw new IllegalStateException("app.well-known.jwk must be defined");
        }
    }
}
