package ee.cyber.cdoc2.server.adapter.conf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.server.app.conf.JwtKeysConf;

@Configuration
public class JwtKeysConfImpl implements JwtKeysConf {
    private final ECPrivateKey ecPrivateKey;
    private final String ecKeyKid;

    public JwtKeysConfImpl(AppProperties props) throws JOSEException, IOException {
        String ecPrivatePem = readFile(props.ecPrivateKeyName());

        this.ecPrivateKey = JWK.parseFromPEMEncodedObjects(ecPrivatePem).toECKey().toECPrivateKey();
        this.ecKeyKid = props.ecKeyKid();
    }


    @ConfigurationProperties(prefix = "app.well-known")
    public record AppProperties(
        String ecPrivateKeyName,
        String ecKeyKid
    ) {
    }

    @Override
    public ECPrivateKey ecPrivateKey() {
        return this.ecPrivateKey;
    }

    @Override
    public String getEcKeyKid() {
        return ecKeyKid;
    }

    private String readFile(String name) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + name);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
