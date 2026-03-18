package ee.cyber.cdoc2.server.app.usecase;

import java.util.UUID;

public interface GetAuthState {
    String execute(UUID uuid);
}
