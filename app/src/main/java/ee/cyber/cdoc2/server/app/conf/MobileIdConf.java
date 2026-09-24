package ee.cyber.cdoc2.server.app.conf;

import ee.sk.mid.MidAuthenticationResponseValidator;

public interface MobileIdConf {
    boolean isAuthenticationResponseValidationEnabled();
    MidAuthenticationResponseValidator getMidAuthenticationResponseValidator();
}
