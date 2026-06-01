package ee.cyber.cdoc2.server.adapter.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

import ee.cyber.cdoc2.server.app.conf.SessionTokenConf;

@Configuration
@EnableConfigurationProperties(SessionTokenConfImpl.AppProperties.class)
public class SessionTokenConfImpl implements SessionTokenConf {

    private final String issuer;

    @Validated
    @ConfigurationProperties(prefix = "app.session-token")
    public record AppProperties(
        @NotNull String issuer
    ) {
    }

    public SessionTokenConfImpl(AppProperties props) {
        this.issuer = props.issuer();
    }

    @Override
    public String getIssuer() {
        return issuer;
    }
}
