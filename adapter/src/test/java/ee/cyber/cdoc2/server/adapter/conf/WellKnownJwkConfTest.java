package ee.cyber.cdoc2.server.adapter.conf;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import ee.cyber.cdoc2.server.adapter.resource.ResourceLoaderWrapper;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WellKnownJwkConfTest {

    private static final String EC_PUBLIC_KEY_PEM = "ec-key-2026.pem";
    private static final String RSA_PUBLIC_KEY_PEM = "rsa-test-public-key.pem";

    private ResourceLoaderWrapper defaultResourceLoader() {
        return new ResourceLoaderWrapper(new DefaultResourceLoader());
    }


    @Test
    void throwsWhenRsaPublicKeyIsConfigured() {
        ResourceLoaderWrapper resourceLoader = defaultResourceLoader();

        assertThrows(
            Exception.class,
            () -> new WellKnownJwkConf(
                new WellKnownJwkConf.AppProperties(
                    List.of(EC_PUBLIC_KEY_PEM, RSA_PUBLIC_KEY_PEM),
                    EC_PUBLIC_KEY_PEM
                ),
                resourceLoader
            )
        );
    }
}
