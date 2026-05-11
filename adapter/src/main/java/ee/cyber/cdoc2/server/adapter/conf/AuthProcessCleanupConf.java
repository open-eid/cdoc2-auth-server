package ee.cyber.cdoc2.server.adapter.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AuthProcessCleanupConf {
    private final int maxAge;

    @ConfigurationProperties(prefix = "app.cleanup")
    public record AppProperties(
        int authProcessMaxAgeMinutes
    ) {
    }

    public AuthProcessCleanupConf(AppProperties props) {
        this.maxAge = props.authProcessMaxAgeMinutes;
    }

    public int getMaxAge() {
        return maxAge;
    }
}
