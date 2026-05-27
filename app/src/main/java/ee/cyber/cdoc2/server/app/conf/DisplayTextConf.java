package ee.cyber.cdoc2.server.app.conf;

import ee.cyber.cdoc2.server.app.usecase.startauth.Language;

public interface DisplayTextConf {
    String getDisplayText(Language language, String semanticsIdentifier);

    Language getDefaultLanguage();
}
