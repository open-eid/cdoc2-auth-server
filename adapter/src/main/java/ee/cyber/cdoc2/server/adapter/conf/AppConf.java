package ee.cyber.cdoc2.server.adapter.conf;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;

@Configuration
@RequiredArgsConstructor
public class AppConf {
    private final SessionNonceUriDbCache sessionNonceUriDbCache;

    @Bean
    public SessionNonceUriConf sessionNonceUriConf() {
        return new SessionNonceUriConfImpl(
            sessionNonceUriDbCache
        );
    }
}
