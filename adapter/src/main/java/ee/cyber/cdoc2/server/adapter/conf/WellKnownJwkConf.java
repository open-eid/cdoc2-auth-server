package ee.cyber.cdoc2.server.adapter.conf;

import java.io.InputStream;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final String activePublicKeyKid;

    @ConfigurationProperties(prefix = "app.well-known")
    public record AppProperties(
        @Nullable List<String> publicKeys,
        @Nullable String activePublicKey
    ) {
    }

    public WellKnownJwkConf(
        AppProperties props,
        ResourceLoaderWrapper resourceLoader
    ) throws Exception {
        validateConf(props);

        this.resourceLoader = resourceLoader;

        Map<String, JWK> jwkMap = toJwkMap(props.publicKeys);
        JWK activePublicKeyJwk = jwkMap.get(props.activePublicKey);

        if (activePublicKeyJwk == null) {
            throw new IllegalStateException("Unable to find matching JWK for configured "
                + "active public key " + props.activePublicKey);
        } else {
            this.activePublicKeyKid = activePublicKeyJwk.getKeyID();
        }

        this.jwkResponse = OBJECT_MAPPER.readValue(
            new JWKSet(jwkMap.values().stream().toList())
                .toString(),
            WellKnownResponse.class
        );
    }

    private Map<String, JWK> toJwkMap(List<String> pemFiles) throws Exception {
        Map<String, JWK> jwkMap = new HashMap<>();

        for (String pemFile : pemFiles) {
            JWK publicKeyJwk = loadPublicKeyJwk(pemFile);
            ECPublicKey ecPublicKey = publicKeyJwk.toECKey().toECPublicKey();
            String kid = deriveKid(ecPublicKey);

            ECKey jwk = new ECKey.Builder(Curve.P_256, publicKeyJwk.toECKey().toECPublicKey())
                .algorithm(JWSAlgorithm.ES256)
                .keyID(kid)
                .build();
            jwkMap.put(pemFile, jwk);
        }

        return jwkMap;
    }

    private static String deriveKid(ECPublicKey publicKey) throws Exception {
        ECKey jwk = new ECKey.Builder(Curve.P_256, publicKey).build();
        return jwk.computeThumbprint().toString();
    }

    private JWK loadPublicKeyJwk(String name) throws Exception {
        try (InputStream is = resourceLoader.loadResource(name).getInputStream()) {
            String pem = new String(is.readAllBytes());
            return JWK.parseFromPEMEncodedObjects(pem);
        }
    }

    public WellKnownResponse getJwkResponse() {
        return jwkResponse;
    }

    public String getActivePublicKeyKid() {
        return activePublicKeyKid;
    }

    private void validateConf(AppProperties props) {
        if (props.publicKeys == null || props.publicKeys.isEmpty()) {
            throw new IllegalStateException("app.well-known.publicKeys must be defined");
        }

        if (props.activePublicKey == null || props.activePublicKey.isBlank()) {
            throw new IllegalStateException("app.well-known.activePublicKey must be defined");
        }

        if (!props.publicKeys.contains(props.activePublicKey)) {
            throw new IllegalStateException("app.well-known.activePublicKey must "
                + "contained in app.well-known.publicKeys list");
        }
    }
}
