package ee.cyber.cdoc2.server.adapter.conf;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;

@Configuration
public class RelyingPartyConfImpl implements RelyingPartyConf {
    private final String name;
    private final UUID uuid;

    public RelyingPartyConfImpl(AppProperties props) {
        this.name = props.name;
        this.uuid = UUID.fromString(props.uuid);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @ConfigurationProperties(prefix = "app.rp")
    public record AppProperties(String name, String uuid) {
    }
}
