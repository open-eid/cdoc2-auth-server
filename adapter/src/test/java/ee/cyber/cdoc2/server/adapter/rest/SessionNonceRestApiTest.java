package ee.cyber.cdoc2.server.adapter.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import ee.cyber.cdoc2.server.adapter.conf.SessionNonceRestClientConf;
import ee.cyber.cdoc2.server.adapter.conf.SessionNonceUriConfImpl;
import ee.cyber.cdoc2.server.app.usecase.startauth.SessionNonce;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class SessionNonceRestApiTest {
    private static final int DEFAULT_TIMEOUT = 500;
    private static final int DEFAULT_RETRIES = 2;
    private static final String SESSION_NONCE_1_URI = "/session_nonce1";
    private static final String SESSION_NONCE_2_URI = "/session_nonce2";
    private static final String SESSION_NONCE_1_RESPONSE = "1234";
    private static final String SESSION_NONCE_2_RESPONSE = "5678";

    private SessionNonceRestApi sessionNonceRestApi;
    private static String baseUrl;

    private static final int WIREMOCK_PORT = 8080;

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(WIREMOCK_PORT))
        .build();

    @BeforeEach
    void setUp() {
        baseUrl = wiremock.baseUrl();
        RestClient restClient = createTestRestClient(DEFAULT_TIMEOUT);

        sessionNonceRestApi = new SessionNonceRestApi(
            restClient,
            new SessionNonceRestClientConf(
                new SessionNonceRestClientConf.AppProperties(DEFAULT_TIMEOUT,
                    DEFAULT_TIMEOUT,
                    DEFAULT_RETRIES
                )),
            new SessionNonceUriConfImpl(new SessionNonceUriConfImpl.AppProperties(
                List.of(baseUrl + SESSION_NONCE_1_URI, baseUrl + SESSION_NONCE_2_URI)
            ))
        );
    }

    @Test
    void collectSessionNoncesReturnsResultWhenAllRequestsOk() {
        stubOkNonce1();
        stubOkNonce2();

        URI sessionNonce1RequestUri = URI.create(baseUrl + SESSION_NONCE_1_URI);
        URI sessionNonce2RequestUri = URI.create(baseUrl + SESSION_NONCE_2_URI);

        List<URI> uris = List.of(
            sessionNonce1RequestUri,
            sessionNonce2RequestUri
        );

        List<SessionNonce.UriSessionNonce> result = sessionNonceRestApi.collectSessionNonces();

        assertEquals(2, result.size());

        Map<URI, String> resultMap = result.stream().collect(Collectors.toMap(
            SessionNonce.UriSessionNonce::uri,
            SessionNonce.UriSessionNonce::nonce)
        );

        assertTrue(resultMap.keySet().containsAll(uris));

        assertEquals(SESSION_NONCE_1_RESPONSE, resultMap.get(sessionNonce1RequestUri));
        assertEquals(SESSION_NONCE_2_RESPONSE, resultMap.get(sessionNonce2RequestUri));
    }

    @Test
    void collectSessionNoncesThrowsExceptionIfARequestTimesOutAfterRetries() {
        stubOkNonce1();
        stubTimeoutNonce2();

//        URI sessionNonce1RequestUri = URI.create(baseUrl + SESSION_NONCE_1_URI);
//        URI sessionNonce2RequestUri = URI.create(baseUrl + SESSION_NONCE_2_URI);
//
//        List<URI> uris = List.of(
//            sessionNonce1RequestUri,
//            sessionNonce2RequestUri
//        );

        Exception exception = assertThrows(
            Exception.class,
            () -> sessionNonceRestApi.collectSessionNonces()
        );

        assertInstanceOf(CompletionException.class, exception);
        assertInstanceOf(ResourceAccessException.class, exception.getCause());
        verify(1, postRequestedFor(urlEqualTo(SESSION_NONCE_1_URI)));
        verify(DEFAULT_RETRIES + 1, postRequestedFor(urlEqualTo(SESSION_NONCE_2_URI)));
    }

    private void stubTimeoutNonce2() {
        stubOkNonce1();
        wiremock.stubFor(
            WireMock.post(
                urlEqualTo(SESSION_NONCE_2_URI)
            ).willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withFixedDelay(DEFAULT_TIMEOUT * 2)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"nonce\":\"" + SESSION_NONCE_2_RESPONSE + "\"}"))
        );
    }

    private void stubOkNonce1() {
        wiremock.stubFor(
            WireMock.post(
                urlEqualTo(SESSION_NONCE_1_URI)
            ).willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("{\"nonce\":\"" + SESSION_NONCE_1_RESPONSE + "\"}"))
        );
    }

    private void stubOkNonce2() {
        wiremock.stubFor(
            WireMock.post(
                urlEqualTo(SESSION_NONCE_2_URI)
            ).willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("{\"nonce\":\"" + SESSION_NONCE_2_RESPONSE + "\"}"))
        );
    }

    private RestClient createTestRestClient(int readTimeout) {
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);

        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build();
    }
}
