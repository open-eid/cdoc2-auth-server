package ee.cyber.cdoc2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import ee.cyber.cdoc2.server.config.MonitoringUtil;

@SpringBootApplication()
@ConfigurationPropertiesScan
public final class Cdoc2AuthServerApplication {
    private Cdoc2AuthServerApplication() {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Cdoc2AuthServerApplication.class);
        // capture startup events for startup actuator endpoint
        app.setApplicationStartup(MonitoringUtil.getApplicationStartupInfo());
        app.run(args);
    }
}
