package ee.cyber.cdoc2.server.app.usecase.startauth;

import ee.sk.smartid.common.notification.interactions.NotificationInteraction;

import java.util.List;
import java.util.UUID;

public interface SidAuthenticate {
    UUID execute(Request request);

    record Request(
        List<NotificationInteraction> interactions,
        byte[] rpChallenge,
        String semanticsIdentifier
    ) {
    }
}
