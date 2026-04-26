package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

public interface StartAuth {
    Response execute(Request request);

    record Request(String nationalId, @Nullable String mobileNr) {
    }

    record Response(UUID uuid, String verificationCode) {
    }
}
