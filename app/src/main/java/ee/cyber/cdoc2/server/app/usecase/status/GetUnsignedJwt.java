package ee.cyber.cdoc2.server.app.usecase.status;

import java.util.UUID;

public interface GetUnsignedJwt {
    String execute(Request request);

    record Request(UUID uuid) {
    }
}
