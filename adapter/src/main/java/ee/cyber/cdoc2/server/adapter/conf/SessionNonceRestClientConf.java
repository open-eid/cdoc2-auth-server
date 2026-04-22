package ee.cyber.cdoc2.server.adapter.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SessionNonceRestClientConf {
    private static final String CONF_DEFAULT_READ_TIMEOUT = "5000";
    private static final String CONF_DEFAULT_CONNECTION_REQUEST_TIMEOUT = "5000";
    private static final String CONF_DEFAULT_SESSION_NONCE_RETRIES = "3";

    private final int retries;

    public SessionNonceRestClientConf(AppProperties props) {
        retries = props.retries;
    }

    @ConfigurationProperties(prefix = "app.restclient.session-nonce")
    public record AppProperties(
        @DefaultValue(CONF_DEFAULT_READ_TIMEOUT) int readTimeout,
        @DefaultValue(CONF_DEFAULT_CONNECTION_REQUEST_TIMEOUT) int connectionRequestTimeout,
        @DefaultValue(CONF_DEFAULT_SESSION_NONCE_RETRIES) int retries
    ) {
    }

    @Bean
    public RestClient sessionNonceRestClient(AppProperties props) {
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(props.readTimeout);
        factory.setConnectionRequestTimeout(props.connectionRequestTimeout);

        return RestClient.builder()
            .requestFactory(factory)
            .build();
    }

    public int getRetries() {
        return retries;
    }
}
