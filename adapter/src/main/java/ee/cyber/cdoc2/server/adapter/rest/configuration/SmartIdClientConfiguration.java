package ee.cyber.cdoc2.server.adapter.rest.configuration;

import ee.sk.smartid.SmartIdClient;
import lombok.RequiredArgsConstructor;

import java.security.KeyStore;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SmartIdClientConfiguration {
    private static final String SSL_BUNDLE_NAME = "sid-trusted-issuers";
    private final SslBundles sslBundles;

    @ConfigurationProperties(prefix = "app.smartid.client")
    public record AppProperties(
        String hostUrl
    ) {
    }

    @Bean
    public SmartIdClient smartIdClient(AppProperties props) {
        KeyStore trustStore = sslBundles.getBundle(SSL_BUNDLE_NAME).getStores().getTrustStore();

        SmartIdClient smartIdClient = new SmartIdClient();
        smartIdClient.setHostUrl(props.hostUrl);
        smartIdClient.setTrustStore(trustStore);

        return smartIdClient;
    }
}
