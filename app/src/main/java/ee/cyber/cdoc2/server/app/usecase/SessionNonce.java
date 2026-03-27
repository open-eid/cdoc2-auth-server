package ee.cyber.cdoc2.server.app.usecase;

import java.net.URI;
import java.util.List;

public interface SessionNonce {
    List<UriSessionNonce> collectSessionNonces();

    record UriSessionNonce(URI uri, String nonce) {
    }
}
