package ee.cyber.cdoc2.server.adapter.conf;

import ee.sk.mid.MidClient;
import lombok.RequiredArgsConstructor;

import java.security.KeyStore;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;

@Configuration
@RequiredArgsConstructor
public class MobileIdClientConf {
    private static final String DEFAULT_DISPLAY_TEXT_FORMAT = "GSM7";
    private static final String DEFAULT_TIMEOUT_SECONDS = "5";

    private static final String SSL_BUNDLE_NAME = "mid-server";
    private final SslBundles sslBundles;

    @ConfigurationProperties(prefix = "app.mobileid.client")
    public record AppProperties(
        String hostUrl,
        @DefaultValue(DEFAULT_TIMEOUT_SECONDS) int timeoutSeconds,
        @DefaultValue(DEFAULT_DISPLAY_TEXT_FORMAT) String displayTextFormat
    ) {
    }

    @Bean
    public MidClient midClient(AppProperties props, RelyingPartyConf relyingPartyConf) {
        KeyStore trustStore = sslBundles.getBundle(SSL_BUNDLE_NAME).getStores().getTrustStore();

        return MidClient.newBuilder()
            .withHostUrl(props.hostUrl)
            .withTrustStore(trustStore)
            .withRelyingPartyName(relyingPartyConf.getMidName())
            .withRelyingPartyUUID(relyingPartyConf.getMidUuid().toString())
            .build();
    }
}
