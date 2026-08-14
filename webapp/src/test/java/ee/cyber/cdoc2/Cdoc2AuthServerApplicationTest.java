package ee.cyber.cdoc2;


import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import com.authlete.sd.SDJWT;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;

import ee.cyber.cdoc2.server.adapter.generated.model.AuthProcessStatusResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class Cdoc2AuthServerApplicationTest extends AbstractAuthServerTest {

    private static final String SID_IDENTIFIER_USER_REFUSED = "PNOEE-30403039917";

    @Tag("net")
    @Test
    void shouldPerformFullAuthProcessForSid() throws Exception {
        performAuthProcess(
            new StartAuthRequest(
                "etsi/" + SID_IDENTIFIER_OK,
                null
            )
        );
    }

    @Tag("net")
    @Test
    void shouldPerformFullAuthProcessForMid() throws Exception {
        performAuthProcess(
            new StartAuthRequest(
                "etsi/" + MID_IDENTIFIER_OK,
                MID_PHONE_NUMBER_OK
            )
        );
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

    @Test
    void shouldGetInfo() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                get(URI.create("/info"))
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        Map<?, ?> info = OBJECT_MAPPER.readValue(response.getContentAsString(), Map.class);

        assertFalse(info.isEmpty());
    }


    private void performAuthProcess(StartAuthRequest startAuthRequest) throws Exception {
        stubSessionNonces();

        MockHttpServletResponse startAuthResponse = mockMvc.perform(
                post(URI.create("/auth/start"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OBJECT_MAPPER.writeValueAsString(startAuthRequest))
            ).andExpect(status().isCreated())
            .andReturn().getResponse();

        AuthProcessStatusResponse authStatusResponseBody = performAuthStatusRequest(
            startAuthResponse,
            5
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
            .filter(k -> k.kid.startsWith("L3RrY5YVq"))
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

        wiremock.verify(postRequestedFor(urlEqualTo(SESSION_NONCE_URI_1)));
        wiremock.verify(postRequestedFor(urlEqualTo(SESSION_NONCE_URI_2)));
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

    private record StartAuthRequest(String identifier, String mobileNr) {
    }

    private record GetWellKnownResponseBody(
        List<WellKnownKey> keys
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WellKnownKey(
        String kid,
        String kty,
        String crv,
        String x,
        String y
    ) {
    }
}
