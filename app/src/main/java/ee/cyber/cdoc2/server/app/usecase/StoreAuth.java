package ee.cyber.cdoc2.server.app.usecase;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StoreAuth {

    void execute(Request request);

    record Request(UUID authUuid, String authState) {
    }
}
