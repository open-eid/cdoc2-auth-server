package ee.cyber.cdoc2.server.adapter.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.DisplayTextConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.Language;

@Configuration
public class DisplayTextConfImpl implements DisplayTextConf {
    private static final String DEFAULT_DISPLAY_TEXT = "Please confirm authentication";

    @ConfigurationProperties(prefix = "app.auth.display-text")
    public record AppProperties(
        @DefaultValue(DEFAULT_DISPLAY_TEXT) String ee,
        @DefaultValue(DEFAULT_DISPLAY_TEXT) String ru,
        @DefaultValue(DEFAULT_DISPLAY_TEXT) String en
    ) {
    }

    private final AppProperties props;

    public DisplayTextConfImpl(AppProperties props) {
        this.props = props;
    }

    @Override
    public String getDisplayText(Language language) {
        return switch (language) {
            case EE -> props.ee();
            case RU -> props.ru();
            case EN -> props.en();
        };
    }
}
