package ee.cyber.cdoc2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.server.app.conf.JwtKeysConf;

import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
    "app.jwt.ecPrivateKeyPem=alt-ec-private.pem",
    "app.well-known.publicKeys=alt-ec-public.pem",
    "app.well-known.activePublicKey=alt-ec-public.pem"
})
@AutoConfigureMockMvc
class ConfigurableEcPrivateKeyTest {

    @Autowired
    private JwtKeysConf jwtKeysConf;

    @Test
    void shouldLoadAlternativePrivateKeyWhenConfigured() throws Exception {
        ECPrivateKey expectedKey = parsePemKey("classpath:alt-ec-private.pem");
        ECPrivateKey defaultKey = parsePemKey("classpath:ec-es256-private.pem");

        assertArrayEquals(
            expectedKey.getEncoded(),
            jwtKeysConf.ecPrivateKey().getEncoded(),
            "JwtKeysConf must load the key from the configured PEM path"
        );

        assertFalse(
            java.util.Arrays.equals(defaultKey.getEncoded(), jwtKeysConf.ecPrivateKey().getEncoded()),
            "The loaded key must differ from the default key"
        );
    }

    private ECPrivateKey parsePemKey(String location) throws IOException, JOSEException {
        try (InputStream is = new DefaultResourceLoader().getResource(location).getInputStream()) {
            String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return JWK.parseFromPEMEncodedObjects(pem).toECKey().toECPrivateKey();
        }
    }
}
