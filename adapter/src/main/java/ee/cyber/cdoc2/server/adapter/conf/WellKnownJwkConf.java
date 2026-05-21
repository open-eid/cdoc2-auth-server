package ee.cyber.cdoc2.server.adapter.conf;

import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.adapter.resource.ResourceLoaderWrapper;

@Configuration
public class WellKnownJwkConf {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ResourceLoaderWrapper resourceLoader;

    private final WellKnownResponse jwkResponse;

    @ConfigurationProperties(prefix = "app.well-known")
    public record AppProperties(
        @Nullable List<String> publicKeys
    ) {
    }

    public WellKnownJwkConf(
        AppProperties props,
        ResourceLoaderWrapper resourceLoader
    ) throws Exception {
        validateConf(props);

        this.resourceLoader = resourceLoader;
        this.jwkResponse = OBJECT_MAPPER.readValue(toJwkSet(props.publicKeys), WellKnownResponse.class);
    }

    private String toJwkSet(List<String> pemFiles) throws Exception {
        List<JWK> keys = new ArrayList<>();

        for (String pemFile : pemFiles) {
            ECPublicKey publicKey = loadPemPublicKey(pemFile);

            ECKey jwk = new ECKey.Builder(Curve.P_256, publicKey)
                .keyID(Paths.get(pemFile).getFileName().toString().replace(".pem", ""))
                .algorithm(JWSAlgorithm.ES256)
                .build();
            keys.add(jwk);
        }

        return new JWKSet(keys).toString();
    }

    private ECPublicKey loadPemPublicKey(String name) throws Exception {
        try (InputStream is = resourceLoader.loadResource(name).getInputStream()) {
            String pem = new String(is.readAllBytes());
            String base64 = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
            byte[] der = Base64.getDecoder().decode(base64);

            return (ECPublicKey) KeyFactory
                .getInstance("EC")
                .generatePublic(new X509EncodedKeySpec(der));
        }
    }

    public WellKnownResponse getJwkResponse() {
        return jwkResponse;
    }

    private void validateConf(AppProperties props) {
        if (props.publicKeys == null || props.publicKeys.isEmpty()) {
            throw new IllegalStateException("app.well-known.publicKeys must be defined");
        }
    }
}
