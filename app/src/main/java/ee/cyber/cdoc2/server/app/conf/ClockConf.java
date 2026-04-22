package ee.cyber.cdoc2.server.app.conf;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConf {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
