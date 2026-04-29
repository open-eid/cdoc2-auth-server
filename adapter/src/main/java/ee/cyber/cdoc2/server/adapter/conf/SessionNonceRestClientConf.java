package ee.cyber.cdoc2.server.adapter.conf;

import lombok.Getter;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Getter
@Configuration
public class SessionNonceRestClientConf {
    private static final String CONF_DEFAULT_READ_TIMEOUT = "5000";
    private static final String CONF_DEFAULT_CONNECTION_REQUEST_TIMEOUT = "5000";
    private static final String CONF_DEFAULT_SESSION_NONCE_RETRIES = "3";

    private final int retries;

    private static final String SSL_BUNDLE_NAME = "session-nonce-server";
    private final SslBundles sslBundles;

    public SessionNonceRestClientConf(AppProperties props, SslBundles sslBundles) {
        retries = props.retries;
        this.sslBundles = sslBundles;
    }

    @ConfigurationProperties(prefix = "app.restclient.session-nonce")
    public record AppProperties(
        @DefaultValue(CONF_DEFAULT_READ_TIMEOUT) int readTimeout,
        @DefaultValue(CONF_DEFAULT_CONNECTION_REQUEST_TIMEOUT) int connectionRequestTimeout,
        @DefaultValue(CONF_DEFAULT_SESSION_NONCE_RETRIES) int retries
    ) {
    }

    @Bean
    public RestClient sessionNonceRestClient(AppProperties props) throws Exception {
        KeyStore trustStore = sslBundles.getBundle(SSL_BUNDLE_NAME).getStores().getTrustStore();

        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(trustStore, null)
            .build();

        PoolingHttpClientConnectionManager connectionManager =
            PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(
                    ClientTlsStrategyBuilder.create()
                        .setSslContext(sslContext)
                        .setTlsVersions(TLS.V_1_3)
                        .buildClassic()
                )
                .setDefaultSocketConfig(
                    SocketConfig.custom()
                        .setSoTimeout(Timeout.ofMilliseconds(props.readTimeout()))
                        .build()
                )
                .setDefaultConnectionConfig(
                    ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(props.connectionRequestTimeout()))
                        .build()
                )
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .build();

        return RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
            .build();
    }
}
