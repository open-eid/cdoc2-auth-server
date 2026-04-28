package ee.cyber.cdoc2.server.app.usecase.status;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

public interface FailAuthProcess {
    void execute(Request request);

    record Request(
        UUID uuid,
        @Nullable String endResult
    ) {
    }
}
