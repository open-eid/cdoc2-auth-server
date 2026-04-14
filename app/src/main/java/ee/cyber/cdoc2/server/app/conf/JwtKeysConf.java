package ee.cyber.cdoc2.server.app.conf;

import java.security.interfaces.ECPrivateKey;

public interface JwtKeysConf {
    ECPrivateKey ecPrivateKey();
    String getEcKeyKid();
}
