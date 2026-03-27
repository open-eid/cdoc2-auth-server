package ee.cyber.cdoc2;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class Cdoc2AuthServerApplicationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(18080))
        .build();

    @RegisterExtension
    static WireMockExtension wiremock2 = WireMockExtension.newInstance()
        .options(wireMockConfig().port(18090))
        .build();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAuthStatus() throws Exception {
        wiremock.stubFor(
            WireMock.post(
                urlEqualTo("/session_nonce")
            ).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"nonce\":\"1234567890987654321\"}"))
        );

        wiremock2.stubFor(
            WireMock.post(
                urlEqualTo("/session_nonce")
            ).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"nonce\":\"98765432123456789\"}"))
        );

        StartAuthRequest startAuthRequest = new StartAuthRequest(
            "ETSI-00223355",
            "1234567890"
        );

        MockHttpServletResponse startAuthResponse = mockMvc.perform(
                post(URI.create("/auth/start"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OBJECT_MAPPER.writeValueAsString(startAuthRequest))
            ).andExpect(status().isCreated())
            .andReturn().getResponse();

        MockHttpServletResponse authStatusResponse = mockMvc.perform(
                get(URI.create(
                    startAuthResponse.getHeader("location")
                ))
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        AuthStatusResponseBody authStatusResponseBody = OBJECT_MAPPER.readValue(
            authStatusResponse.getContentAsString(),
            AuthStatusResponseBody.class
        );

        assertEquals("STARTED", authStatusResponseBody.status);
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

    private record AuthStatusResponseBody(String status) {
    }

    private record StartAuthRequest(String identifier, String mobileNr) {
    }

    private record GetWellKnownResponseBody(
        List<WellKnownKeys> keys
    ) {
    }

    private record WellKnownKeys(
        String kid,
        String kty
    ) {
    }
}
