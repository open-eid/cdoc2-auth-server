package ee.cyber.cdoc2.server.adapter.conf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.nimbusds.jose.JOSEException;

import ee.cyber.cdoc2.server.adapter.resource.ResourceLoaderWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtKeyConfImplTest {

    private static final String EC_PRIVATE_KEY_PEM = "ec-es256-private.pem";
    private static final String EC_PUBLIC_KEY_PEM = "ec-key-2026.pem";

    private ResourceLoaderWrapper defaultResourceLoader() {
        return new ResourceLoaderWrapper(new DefaultResourceLoader());
    }

    @Test
    void loadsEcPrivateKeyAndKidSuccessfully() throws Exception {
        ResourceLoaderWrapper resourceLoader = defaultResourceLoader();
        WellKnownJwkConf wellKnownJwkConf = new WellKnownJwkConf(
            new WellKnownJwkConf.AppProperties(List.of(EC_PUBLIC_KEY_PEM), EC_PUBLIC_KEY_PEM),
            resourceLoader
        );
        JwtKeyConfImpl.AppProperties props = new JwtKeyConfImpl.AppProperties(EC_PRIVATE_KEY_PEM);

        JwtKeyConfImpl conf = new JwtKeyConfImpl(props, resourceLoader, wellKnownJwkConf);

        assertInstanceOf(ECPrivateKey.class, conf.ecPrivateKey());
        assertEquals(wellKnownJwkConf.getActivePublicKeyKid(), conf.getKid());
    }

    @Test
    void throwsWhenEcPrivateKeyPemIsNull() {
        JwtKeyConfImpl.AppProperties props = new JwtKeyConfImpl.AppProperties(null);

        assertThrows(
            IllegalStateException.class,
            () -> new JwtKeyConfImpl(props, null, null)
        );
    }

    @Test
    void throwsWhenEcPrivateKeyPemIsBlank() {
        JwtKeyConfImpl.AppProperties props = new JwtKeyConfImpl.AppProperties("   ");

        assertThrows(
            IllegalStateException.class,
            () -> new JwtKeyConfImpl(props, null, null)
        );
    }

    @Test
    void throwsWhenPemFileDoesNotExist() {
        ResourceLoaderWrapper resourceLoader = defaultResourceLoader();
        JwtKeyConfImpl.AppProperties props = new JwtKeyConfImpl.AppProperties("nonexistent-key.pem");

        assertThrows(
            IOException.class,
            () -> new JwtKeyConfImpl(props, resourceLoader, null)
        );
    }

    @Test
    void throwsWhenPemContentIsInvalid() {
        ResourceLoaderWrapper invalidPemLoader = new ResourceLoaderWrapper(new ResourceLoader() {
            @Override
            public Resource getResource(String location) {
                return new ByteArrayResource("not valid pem content".getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public ClassLoader getClassLoader() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        JwtKeyConfImpl.AppProperties props = new JwtKeyConfImpl.AppProperties("any.pem");

        assertThrows(
            JOSEException.class,
            () -> new JwtKeyConfImpl(props, invalidPemLoader, null)
        );
    }
}
