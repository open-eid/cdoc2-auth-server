package ee.cyber.cdoc2.server.adapter.rest;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ee.cyber.cdoc2.server.adapter.conf.SessionNonceRestClientConf;
import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.SessionNonce;

@Component
public class SessionNonceRestApi implements SessionNonce {
    private final RestClient restClient;
    int retries;
    private final SessionNonceUriConf sessionNonceUriConf;

    public SessionNonceRestApi(
        @Qualifier(value = "sessionNonceRestClient") RestClient restClient,
        SessionNonceRestClientConf restClientConf,
        SessionNonceUriConf uriConf
    ) {
        this.restClient = restClient;
        this.retries = restClientConf.getRetries();
        this.sessionNonceUriConf = uriConf;
    }

    @Override
    public List<UriSessionNonce> collectSessionNonces() {
        List<Supplier<CompletableFuture<UriSessionNonce>>> collectNonceTasks = sessionNonceUriConf
            .getUris().stream()
            .map(
                this::createSupplier
            ).toList();

        return PerformTaskWithRetriesHelper.allOfWithRetries(collectNonceTasks, this.retries);
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
