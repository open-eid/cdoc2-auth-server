package ee.cyber.cdoc2;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.authlete.sd.SDJWT;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;

import ee.cyber.cdoc2.server.adapter.generated.model.AuthProcessStatusResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class Cdoc2AuthServerApplicationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int WIREMOCK_PORT = 8080;
    private static final String IDENTIFIER_OK = "PNOEE-40504040001";
    private static final String IDENTIFIER_USER_REFUSED = "PNOEE-30403039917";
    private static final String SESSION_NONCE_1_VALUE = "WTq9gAkv5_UJioELXDqOAA";
    private static final String SESSION_NONCE_2_VALUE = "nrVcSEcHuWt2SKfjkMm6RQ";
    public static final String SESSION_NONCE_URI_1 = "/session_nonce_1";
    public static final String SESSION_NONCE_URI_2 = "/session_nonce_2";

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(WIREMOCK_PORT))
        .build();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAuthStatus() throws Exception {
        wiremock.stubFor(
            WireMock.post(
                urlEqualTo(SESSION_NONCE_URI_1)
            ).willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("{\"nonce\":\"" + SESSION_NONCE_1_VALUE + "\"}"))
        );

        wiremock.stubFor(
            WireMock.post(
                urlEqualTo(SESSION_NONCE_URI_2)
            ).willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("{\"nonce\":\"" + SESSION_NONCE_2_VALUE + "\"}"))
        );

        StartAuthRequest startAuthRequest = new StartAuthRequest(
            "etsi/" + IDENTIFIER_OK,
            null
        );

        MockHttpServletResponse startAuthResponse = mockMvc.perform(
                post(URI.create("/auth/start"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OBJECT_MAPPER.writeValueAsString(startAuthRequest))
            ).andExpect(status().isCreated())
            .andReturn().getResponse();

        AuthProcessStatusResponse authStatusResponseBody = performAuthStatusRequest(
            startAuthResponse,
            2
        );

        assertNotNull(authStatusResponseBody);
        assertEquals("COMPLETE", authStatusResponseBody.getStatus());

        MockHttpServletResponse getWellKnownResponse = mockMvc.perform(
                get(URI.create("/.well-known/jwks.jws"))
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        GetWellKnownResponseBody getWellKnownResponseBody = OBJECT_MAPPER.readValue(
            getWellKnownResponse.getContentAsString(),
            GetWellKnownResponseBody.class
        );

        WellKnownKey ecPublicKey = getWellKnownResponseBody.keys.stream()
            .filter(k -> "ec-key-2026".equals(k.kid))
            .findFirst().orElse(null);

        assertNotNull(ecPublicKey);

        String jwk = OBJECT_MAPPER.writeValueAsString(ecPublicKey);

        JWK ecPublicJWK = JWK.parse(jwk);

        ECKey ecKey = (ECKey) ecPublicJWK;
        ECPublicKey publicKey = ecKey.toECPublicKey();

        SDJWT sdjwt = SDJWT.parse(authStatusResponseBody.getSessionToken());
        SignedJWT signedJWT = SignedJWT.parse(sdjwt.getCredentialJwt());

        JWSVerifier verifier = new ECDSAVerifier(publicKey);

        assertTrue(signedJWT.verify(verifier));

        verify(postRequestedFor(urlEqualTo(SESSION_NONCE_URI_1)));
        verify(postRequestedFor(urlEqualTo(SESSION_NONCE_URI_2)));
    }

    private AuthProcessStatusResponse performAuthStatusRequest(
        MockHttpServletResponse startAuthResponse,
        int maxPollCount
    ) throws Exception {
        assertNotNull(startAuthResponse);
        AuthProcessStatusResponse authStatusResponseBody = null;

        for (int i = 0; i < maxPollCount; i++) {
            MockHttpServletResponse authStatusResponse = mockMvc.perform(
                    get(URI.create(
                        startAuthResponse.getHeader("location")
                    ))
                ).andExpect(status().isOk())
                .andReturn().getResponse();

            authStatusResponseBody = OBJECT_MAPPER.readValue(
                authStatusResponse.getContentAsString(),
                AuthProcessStatusResponse.class
            );

            if (!"STARTED".equals(authStatusResponseBody.getStatus())) {
                break;
            }
        }

        return authStatusResponseBody;
    }

    @Test
    void shouldGetWellKnown() throws Exception {
        MockHttpServletResponse getWellKnownResponse = mockMvc.perform(
                get(URI.create("/.well-known/jwks.jws"))
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        GetWellKnownResponseBody getWellKnownResponseBody = OBJECT_MAPPER.readValue(
            getWellKnownResponse.getContentAsString(),
            GetWellKnownResponseBody.class
        );

        assertEquals(2, getWellKnownResponseBody.keys.size());

        assertTrue(
            getWellKnownResponseBody.keys.stream()
                .allMatch(key -> key.kid != null && key.kty != null)
        );
    }

    private record StartAuthRequest(String identifier, String mobileNr) {
    }

    private record GetWellKnownResponseBody(
        List<WellKnownKey> keys
    ) {
    }

    private record WellKnownKey(
        String kid,
        String kty,
        String crv,
        String x,
        String y
    ) {
    }
}
