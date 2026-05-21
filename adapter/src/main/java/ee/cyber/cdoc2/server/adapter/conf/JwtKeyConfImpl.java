package ee.cyber.cdoc2.server.adapter.conf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.server.adapter.resource.ResourceLoaderWrapper;
import ee.cyber.cdoc2.server.app.conf.JwtKeysConf;

@Configuration
public class JwtKeyConfImpl implements JwtKeysConf {
    private final ResourceLoaderWrapper resourceLoader;
    private final ECPrivateKey ecPrivateKey;
    private final String kid;

    @ConfigurationProperties(prefix = "app.jwt")
    public record AppProperties(
        @Nullable String ecPrivateKeyPem,
        @Nullable String kid
    ) {
    }

    public JwtKeyConfImpl(
        AppProperties props,
        ResourceLoaderWrapper resourceLoader
    ) throws JOSEException,
        IOException {
        validateConf(props);
        this.resourceLoader = resourceLoader;
        String ecPrivatePem = readFile(props.ecPrivateKeyPem());
        this.ecPrivateKey = JWK.parseFromPEMEncodedObjects(ecPrivatePem).toECKey().toECPrivateKey();
        this.kid = props.kid();
    }

    @Override
    public ECPrivateKey ecPrivateKey() {
        return this.ecPrivateKey;
    }

    @Override
    public String getKid() {
        return kid;
    }

    private String readFile(String name) throws IOException {
        try (InputStream is = resourceLoader.loadResource(name).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void validateConf(AppProperties props) {
        if (props.ecPrivateKeyPem == null || props.ecPrivateKeyPem.isBlank()) {
            throw new IllegalStateException("app.jwt.ecPrivateKeyPem must be defined");
        }

        if (props.kid == null || props.kid.isBlank()) {
            throw new IllegalStateException("app.jwt.kid must be defined");
        }
    }
}
