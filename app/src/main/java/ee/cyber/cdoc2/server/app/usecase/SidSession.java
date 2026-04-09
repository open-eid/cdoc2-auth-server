package ee.cyber.cdoc2.server.app.usecase;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface SidSession {

    Response execute(UUID sessionId);

    record Response(
        String state,
        String endResult,
        @Nullable SessionSignature signature
    ) {
        boolean isCompletedOk() {
            return "COMPLETE".equals(state) && "OK".equals(endResult);
        }

        boolean isCompletedNotOk() {
            return "COMPLETE".equals(state) && !"OK".equals(endResult);
        }
    }

    record SessionSignature(
        String value,
        String serverRandom,
        String userChallenge
    ) {
    }

}
