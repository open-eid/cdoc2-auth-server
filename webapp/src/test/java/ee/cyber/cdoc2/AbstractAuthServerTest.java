package ee.cyber.cdoc2;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractAuthServerTest {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int WIREMOCK_PORT = 9080;

    static final String SID_IDENTIFIER_OK = "PNOEE-40504040001";
    public static final String MID_IDENTIFIER_OK = "PNOEE-51307149560";
    public static final String MID_PHONE_NUMBER_OK = "+37269930366";

    static final String SESSION_NONCE_1_VALUE = "WTq9gAkv5_UJioELXDqOAA";
    static final String SESSION_NONCE_2_VALUE = "nrVcSEcHuWt2SKfjkMm6RQ";
    public static final String SESSION_NONCE_URI_1 = "/session_nonce_1";
    public static final String SESSION_NONCE_URI_2 = "/session_nonce_2";

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(wireMockConfig()
            .httpDisabled(true)
            .httpsPort(WIREMOCK_PORT)
            .keystorePath("wiremock_keystore.p12")
            .keystorePassword("changeit")
            .keyManagerPassword("changeit")
            .keystoreType("PKCS12")
        )
        .build();

    @Autowired
    protected MockMvc mockMvc;

    void stubSessionNonces() {
        wiremock.stubFor(
            WireMock.post(urlEqualTo(SESSION_NONCE_URI_1))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"nonce\":\"" + SESSION_NONCE_1_VALUE + "\"}"))
        );

        wiremock.stubFor(
            WireMock.post(urlEqualTo(SESSION_NONCE_URI_2))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"nonce\":\"" + SESSION_NONCE_2_VALUE + "\"}"))
        );
    }
}
