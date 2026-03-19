package ee.cyber.cdoc2.server.app.usecase;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface StartAuth {
    UUID execute(Request request);

    record Request(String nationalId, @Nullable String mobileNr) {
    }
}
