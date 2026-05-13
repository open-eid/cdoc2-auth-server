package ee.cyber.cdoc2.server.app.usecase.status.mid;

import ee.sk.mid.rest.dao.MidSessionSignature;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

public interface GetMidSession {

    Response execute(UUID sessionId);

    record Response(
        String state,
        @Nullable String endResult,
        @Nullable String cert,
        @Nullable MidSessionSignature signature
    ) {
    }
}
