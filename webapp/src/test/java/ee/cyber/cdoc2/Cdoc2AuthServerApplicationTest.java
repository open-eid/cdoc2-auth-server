package ee.cyber.cdoc2;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class Cdoc2AuthServerApplicationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAuthStatus() throws Exception {
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

    private record AuthStatusResponseBody(String status) {
    }

    private record StartAuthRequest(String identifier, String mobileNr) {
    }
}
