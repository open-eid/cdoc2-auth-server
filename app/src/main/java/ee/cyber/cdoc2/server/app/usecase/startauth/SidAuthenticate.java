package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.util.UUID;

public interface SidAuthenticate {
    UUID execute(Request request);

    record Request(
        String interactions,
        byte[] rpChallenge,
        String semanticsIdentifier
    ) {
    }
}
