package ee.cyber.cdoc2.server.app.conf;

import java.util.UUID;

import ee.cyber.cdoc2.server.app.CertificateLevel;

public interface RelyingPartyConf {
    String getName();

    UUID getUuid();

    CertificateLevel getCertificateLevel();
}
