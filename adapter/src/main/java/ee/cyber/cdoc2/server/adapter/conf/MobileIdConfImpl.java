package ee.cyber.cdoc2.server.adapter.conf;

import ee.sk.mid.MidAuthenticationResponseValidator;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyStore;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.MobileIdConf;

@Slf4j
@Configuration(proxyBeanMethods = false)
public final class MobileIdConfImpl implements MobileIdConf {
    private static final String DEFAULT_SIGNATURE_VERIFICATION_ENABLED = "false";

    private static final String SSL_BUNDLE_NAME = "trusted-issuers";
    private final boolean isAuthenticationResponseValidationEnabled;
    @Nullable
    private final MidAuthenticationResponseValidator midAuthenticationResponseValidator;

    public MobileIdConfImpl(SslBundles sslBundles, AppProperties props) {
        this.isAuthenticationResponseValidationEnabled =
            props.validateAuthenticationResponse;
        if (this.isAuthenticationResponseValidationEnabled) {
            KeyStore trustStore = sslBundles.getBundle(SSL_BUNDLE_NAME).getStores().getTrustStore();
            this.midAuthenticationResponseValidator =
                new MidAuthenticationResponseValidator(trustStore);
        } else {
            this.midAuthenticationResponseValidator = null;
        }
    }

    @Override
    public boolean isAuthenticationResponseValidationEnabled() {
        return this.isAuthenticationResponseValidationEnabled;
    }

    @Override
    public MidAuthenticationResponseValidator getMidAuthenticationResponseValidator() {
        if (this.midAuthenticationResponseValidator == null) {
            String message = "Attempting to access null MidAuthenticationResponseValidator. "
                + "validateAuthenticationResponse = "
                + this.isAuthenticationResponseValidationEnabled;

            log.error(message);

            throw new IllegalStateException(message);
        }
        return this.midAuthenticationResponseValidator;
    }

    @ConfigurationProperties(prefix = "app.mobileid")
    public record AppProperties(
        @DefaultValue(
            DEFAULT_SIGNATURE_VERIFICATION_ENABLED
        ) boolean validateAuthenticationResponse
    ) {
    }
}
