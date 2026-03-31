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

    @Override
    public List<UriSessionNonce> collectSessionNonces(List<URI> uris) {
        List<Supplier<CompletableFuture<UriSessionNonce>>> collectNonceTasks = uris.stream().map(
            this::createSupplier
        ).toList();

        return PerformTaskWithRetriesHelper.allOfWithRetries(collectNonceTasks, props.retries());
    }

    private Supplier<CompletableFuture<UriSessionNonce>> createSupplier(URI uri) {
        return () -> sessionNonceFutureForUri(uri);
    }

    private CompletableFuture<UriSessionNonce> sessionNonceFutureForUri(URI uri) {
        return CompletableFuture.supplyAsync(() -> createSessionNonce(uri)
        );
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

    private record NonceBody(String nonce) {
    }
}
