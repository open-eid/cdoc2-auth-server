package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StoreAuthProcess {

    void execute(Request request);

    record Request(
        UUID authUuid,
        UUID midSidSessionId,
        List<SessionNonce.UriSessionNonce> sessionNonces,
        String unsignedSdJwt,
        String interactionsDigest
    ) {
    }
}
