package ee.cyber.cdoc2.server.adapter.rest.configuration;

import ee.sk.smartid.SmartIdClient;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import ee.cyber.cdoc2.server.adapter.resource.ResourceLoaderWrapper;

@Configuration
@RequiredArgsConstructor
public class SmartIdClientConfiguration {
    private final ResourceLoaderWrapper resourceLoader;

    @ConfigurationProperties(prefix = "app.smartid.client")
    public record AppProperties(
        String hostUrl,
        SSL ssl
    ) {

        public record SSL(
            String trustStore,
            String trustStorePassword
        ) {
        }
    }

    @Bean
    public SmartIdClient smartIdClient(AppProperties props)
        throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        Resource trustStoreResource = resourceLoader.loadResource(props.ssl.trustStore);

        InputStream is = trustStoreResource.getInputStream();
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, props.ssl.trustStorePassword.toCharArray());

        SmartIdClient smartIdClient = new SmartIdClient();
        smartIdClient.setHostUrl(props.hostUrl);
        smartIdClient.setTrustStore(trustStore);

        return smartIdClient;
    }
}
