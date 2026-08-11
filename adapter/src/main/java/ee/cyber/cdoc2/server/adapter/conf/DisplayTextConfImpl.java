package ee.cyber.cdoc2.server.adapter.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.DisplayTextConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.Language;

@Configuration
public class DisplayTextConfImpl implements DisplayTextConf {

    static final String SEMANTICS_IDENTIFIER_PLACEHOLDER = "{semanticsIdentifier}";
    private static final String DEFAULT_DISPLAY_TEXT =
        "Please confirm authentication: " + SEMANTICS_IDENTIFIER_PLACEHOLDER;

    @ConfigurationProperties(prefix = "app.auth.display-text")
    public record AppProperties(
        @DefaultValue(DEFAULT_DISPLAY_TEXT) String et,
        @DefaultValue(DEFAULT_DISPLAY_TEXT) String ru,
        @DefaultValue(DEFAULT_DISPLAY_TEXT) String en,
        @DefaultValue(DEFAULT_DISPLAY_TEXT) String lt,
        @DefaultValue("et") Language defaultLanguage
    ) {
    }

    private final AppProperties props;

    public DisplayTextConfImpl(AppProperties props) {
        this.props = props;
    }

    @Override
    public String getDisplayText(Language language, String semanticsIdentifier) {
        String template = switch (language) {
            case ET -> props.et();
            case RU -> props.ru();
            case EN -> props.en();
            case LT -> props.lt();
        };

        return template.replace(SEMANTICS_IDENTIFIER_PLACEHOLDER, semanticsIdentifier);
    }

    @Override
    public Language getDefaultLanguage() {
        return props.defaultLanguage();
    }
}
