package ee.cyber.cdoc2.server.adapter.conf;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.CertificateLevel;
import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;

@Configuration
public class RelyingPartyConfImpl implements RelyingPartyConf {
    private final String name;
    private final UUID uuid;
    private final CertificateLevel certificateLevel;
    private final String schemeName;

    @ConfigurationProperties(prefix = "app.rp")
    public record AppProperties(
        String name,
        String uuid,
        @DefaultValue("ADVANCED") String certificateLevel,
        @DefaultValue("smart-id-demo") String schemeName
    ) {
    }

    public RelyingPartyConfImpl(AppProperties props) {
        this.name = props.name;
        this.uuid = UUID.fromString(props.uuid);
        this.certificateLevel = CertificateLevel.valueOf(props.certificateLevel);
        this.schemeName = props.schemeName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public CertificateLevel getCertificateLevel() {
        return this.certificateLevel;
    }

    @Override
    public String getSchemeName() {
        return schemeName;
    }
}
