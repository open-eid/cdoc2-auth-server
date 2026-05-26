package ee.cyber.cdoc2;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ee.cyber.cdoc2.server.adapter.rest.MidRestClient;
import ee.cyber.cdoc2.server.adapter.rest.SidRestClient;
import ee.cyber.cdoc2.server.app.usecase.startauth.Language;
import ee.cyber.cdoc2.server.app.usecase.startauth.MidAuthenticate;
import ee.cyber.cdoc2.server.app.usecase.startauth.SidAuthenticate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DisplayTextLanguageTest extends AbstractAuthServerTest {

    @MockitoBean
    private SidRestClient sidRestClient;

    @MockitoBean
    private MidRestClient midRestClient;

    @BeforeEach
    void setUp() {
        stubSessionNonces();

        when(sidRestClient.execute(any(SidAuthenticate.Request.class)))
            .thenReturn(UUID.randomUUID());
        when(midRestClient.execute(any(MidAuthenticate.Request.class)))
            .thenReturn(UUID.randomUUID());
    }

    @Test
    void shouldUseSidEstonianDisplayTextWhenLanguageIsEE() throws Exception {
        startSidAuth("ee");

        ArgumentCaptor<SidAuthenticate.Request> captor =
            ArgumentCaptor.forClass(SidAuthenticate.Request.class);
        verify(sidRestClient).execute(captor.capture());

        // confirmationMessageAndVerificationCodeChoice sets the displayText200 field
        String displayText = captor.getValue().interactions().get(0).displayText200();
        assertEquals("Kinnitage autentimine: " + SID_IDENTIFIER_OK, displayText);
    }

    @Test
    void shouldUseSidEnglishDisplayTextWhenLanguageIsEN() throws Exception {
        startSidAuth("en");

        ArgumentCaptor<SidAuthenticate.Request> captor =
            ArgumentCaptor.forClass(SidAuthenticate.Request.class);
        verify(sidRestClient).execute(captor.capture());

        String displayText = captor.getValue().interactions().get(0).displayText200();
        assertEquals("Please confirm authentication: " + SID_IDENTIFIER_OK, displayText);
    }

    @Test
    void shouldUseSidRussianDisplayTextWhenLanguageIsRU() throws Exception {
        startSidAuth("ru");

        ArgumentCaptor<SidAuthenticate.Request> captor =
            ArgumentCaptor.forClass(SidAuthenticate.Request.class);
        verify(sidRestClient).execute(captor.capture());

        String displayText = captor.getValue().interactions().get(0).displayText200();
        assertEquals("Подтвердите аутентификацию: " + SID_IDENTIFIER_OK, displayText);
    }

    @Test
    void shouldUseMidEstonianDisplayTextAndLanguageWhenLanguageIsEE() throws Exception {
        startMidAuth("ee");

        ArgumentCaptor<MidAuthenticate.Request> captor =
            ArgumentCaptor.forClass(MidAuthenticate.Request.class);
        verify(midRestClient).execute(captor.capture());

        MidAuthenticate.Request request = captor.getValue();
        assertEquals("Kinnitage autentimine: " + MID_IDENTIFIER_OK, request.displayText());
        assertEquals(Language.EE, request.language());
    }

    @Test
    void shouldUseMidEnglishDisplayTextAndLanguageWhenLanguageIsEN() throws Exception {
        startMidAuth("en");

        ArgumentCaptor<MidAuthenticate.Request> captor =
            ArgumentCaptor.forClass(MidAuthenticate.Request.class);
        verify(midRestClient).execute(captor.capture());

        MidAuthenticate.Request request = captor.getValue();
        assertEquals("Please confirm authentication: " + MID_IDENTIFIER_OK, request.displayText());
        assertEquals(Language.EN, request.language());
    }

    @Test
    void shouldUseMidRussianDisplayTextAndLanguageWhenLanguageIsRU() throws Exception {
        startMidAuth("ru");

        ArgumentCaptor<MidAuthenticate.Request> captor =
            ArgumentCaptor.forClass(MidAuthenticate.Request.class);
        verify(midRestClient).execute(captor.capture());

        MidAuthenticate.Request request = captor.getValue();
        assertEquals("Подтвердите аутентификацию: " + MID_IDENTIFIER_OK, request.displayText());
        assertEquals(Language.RU, request.language());
    }

    private void startSidAuth(String language) throws Exception {
        mockMvc.perform(
            post(URI.create("/auth/start"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(
                    new StartAuthRequest("etsi/" + SID_IDENTIFIER_OK, null, language)
                ))
        ).andExpect(status().isCreated());
    }

    private void startMidAuth(String language) throws Exception {
        mockMvc.perform(
            post(URI.create("/auth/start"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(
                    new StartAuthRequest("etsi/" + MID_IDENTIFIER_OK, MID_PHONE_NUMBER_OK, language)
                ))
        ).andExpect(status().isCreated());
    }

    private record StartAuthRequest(String identifier, String mobileNr, String language) {
    }
}
