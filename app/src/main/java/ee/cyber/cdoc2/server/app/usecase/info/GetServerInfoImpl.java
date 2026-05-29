package ee.cyber.cdoc2.server.app.usecase.info;


import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class GetServerInfoImpl implements GetServerInfo {

    private final List<InfoContributor> infoContributors;

    @Override
    public Map<String, Object> execute() {
        Info.Builder builder = new Info.Builder();
        infoContributors.forEach(c -> c.contribute(builder));
        return builder.build().getDetails();
    }
}

