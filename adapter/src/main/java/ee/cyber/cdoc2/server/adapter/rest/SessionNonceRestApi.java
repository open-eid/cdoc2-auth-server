package ee.cyber.cdoc2.server.adapter.rest;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ee.cyber.cdoc2.server.adapter.rest.configuration.SessionNonceRestClientConfiguration;
import ee.cyber.cdoc2.server.app.usecase.SessionNonce;

@Component
public class SessionNonceRestApi implements SessionNonce {
    private final RestClient restClient;
    private final SessionNonceRestClientConfiguration.AppProperties props;

    public SessionNonceRestApi(
        @Qualifier(value = "sessionNonceRestClient") RestClient restClient,
        SessionNonceRestClientConfiguration.AppProperties props
    ) {
        this.restClient = restClient;
        this.props = props;
    }

    private UriSessionNonce createSessionNonce(URI uri) {
        NonceBody nonceBody = restClient.post()
            .uri(uri)
            .retrieve()
            .body(NonceBody.class);

        if (nonceBody == null || nonceBody.nonce == null) {
            throw new IllegalStateException("malformed nonce response");
        }

        return new UriSessionNonce(
            uri,
            nonceBody.nonce
        );
    }

    @Override
    public List<UriSessionNonce> collectSessionNonces() {
        List<Supplier<CompletableFuture<UriSessionNonce>>> collectNonceTasks = List.of(
            () -> sessionNonceFutureForUri(URI.create("http://localhost:18080/session_nonce")),
            () -> sessionNonceFutureForUri(URI.create("http://localhost:18090/session_nonce"))
        );

        return PerformTaskWithRetriesHelper.allOfWithRetries(collectNonceTasks, props.retries());
    }

    private CompletableFuture<UriSessionNonce> sessionNonceFutureForUri(URI uri) {
        return CompletableFuture.supplyAsync(() -> createSessionNonce(uri)
        );
    }

    private record NonceBody(String nonce) {
    }
}
