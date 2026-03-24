package ee.cyber.cdoc2.server.app.usecase;


import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

@NullMarked
@RequiredArgsConstructor
@Component
public class StartAuthImpl implements StartAuth {
    private final StoreAuth storeAuth;

    @Override
    public UUID execute(Request request) {
        UUID authUuid = UUID.randomUUID();

        storeAuth.execute(new StoreAuth.Request(
            authUuid,
            UUID.randomUUID().toString(),
            AuthProcessStatus.STARTED
        ));

        return authUuid;
    }
}
