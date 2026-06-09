package ee.cyber.cdoc2.server.adapter.rest;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import ee.cyber.cdoc2.server.adapter.conf.SessionNonceRestClientConf;
import ee.cyber.cdoc2.server.adapter.exception.ServerException;
import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.SessionNonce;

@Slf4j
@Component
public class SessionNonceRestApi implements SessionNonce {
    private static final String SESSION_NONCE_NETWORK_ERROR_CODE =
        "CDOC2_NETWORK_ERROR";
    private static final String SESSION_NONCE_MALFORMED_RESPONSE_ERROR_CODE =
        "CDOC2_MALFORMED_RESPONSSE_ERROR";

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
        NonceBody nonceBody;

        try {
            nonceBody = restClient.post()
                .uri(uri)
                .retrieve()
                .body(NonceBody.class);
        } catch (RestClientException e) {
            log.error(e.toString());

            throw new ServerException(
                SESSION_NONCE_NETWORK_ERROR_CODE,
                "error communicating with server: " + uri.getHost()
            );
        }

        if (nonceBody == null || nonceBody.nonce == null) {
            log.error("malformed session nonce response: {}", uri);

            throw new ServerException(
                SESSION_NONCE_MALFORMED_RESPONSE_ERROR_CODE,
                "malformed response received from: " + uri.getHost()
            );
        }

        return new UriSessionNonce(
            uri,
            nonceBody.nonce
        );
    }

    private record NonceBody(
        @Nullable String nonce
    ) {
    }
}
