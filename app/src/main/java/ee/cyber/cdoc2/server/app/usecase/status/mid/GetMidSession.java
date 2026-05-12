package ee.cyber.cdoc2.server.app.usecase.status.mid;

import java.util.UUID;

public interface GetMidSession {

    Response execute(UUID sessionId);

    record Response(String state, String endResult, String cert) {
    }
}
