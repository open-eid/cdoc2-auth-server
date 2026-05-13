package ee.cyber.cdoc2.server.app.conf;

import ee.sk.mid.MidAuthenticationResponseValidator;

import java.security.KeyStore;

import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidConf {
    private static final String SSL_BUNDLE_NAME = "mid-server";

    @Bean
    public MidAuthenticationResponseValidator midAuthenticationResponseValidator(
        SslBundles sslBundles
    ) {
        KeyStore trustStore = sslBundles.getBundle(SSL_BUNDLE_NAME).getStores().getTrustStore();
        return new MidAuthenticationResponseValidator(trustStore);
    }

}
