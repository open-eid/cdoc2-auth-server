package ee.cyber.cdoc2.server.adapter.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AuthProcessCleanupConf {
    private static final String MAX_AGE_MINUTES = "5";
    private static final String DELETION_LIMIT_DEFAULT = "1000";
    private final int maxAge;
    private final int deletionLimit;

    @ConfigurationProperties(prefix = "app.cleanup")
    public record AppProperties(
        @DefaultValue(MAX_AGE_MINUTES) int authProcessMaxAgeMinutes,
        @DefaultValue(DELETION_LIMIT_DEFAULT) int authProcessDeletionLimit
    ) {
    }

    public AuthProcessCleanupConf(AppProperties props) {
        this.maxAge = props.authProcessMaxAgeMinutes;
        this.deletionLimit = props.authProcessDeletionLimit;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public int getDeletionLimit() {
        return deletionLimit;
    }
}
