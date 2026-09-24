package ee.cyber.cdoc2.server.app.usecase.status.mid;

import ee.sk.mid.MidAuthentication;
import ee.sk.mid.MidAuthenticationResponseValidator;
import ee.sk.mid.MidAuthenticationResult;
import ee.sk.mid.rest.dao.MidSessionSignature;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.server.app.conf.MobileIdConf;
import ee.cyber.cdoc2.server.app.usecase.status.GetSessionTokenMaterial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CreateMidSessionTokenTest {

    private static final String MID_CERT_URL_SAFE =
        "MIIDqDCCAy6gAwIBAgIQB9W11BzBABj-0d_AZx6UHzAKBggqhkjOPQQDAjBxMQsw"
            + "CQYDVQQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRh"
            + "DA5OVFJFRS0xMDc0NzAxMzEsMCoGA1UEAwwjVEVTVCBvZiBTSyBJRCBTb2x1dGlv"
            + "bnMgRUlELVEgMjAyMUUwHhcNMjQwNjEyMDY0NTI4WhcNMjkwNjE2MDY0NTI3WjCB"
            + "lTELMAkGA1UEBhMCRUUxLzAtBgNVBAMMJk1BUlkgw4ROTixPJ0NPTk5Fxb0txaBV"
            + "U0xJSyBURVNUTlVNQkVSMSUwIwYDVQQEDBxPJ0NPTk5Fxb0txaBVU0xJSyBURVNU"
            + "TlVNQkVSMRIwEAYDVQQqDAlNQVJZIMOETk4xGjAYBgNVBAUTEVBOT0VFLTUxMzA3"
            + "MTQ5NTYwMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWlV1aVSXw6WhagWmFmXE"
            + "_oe-0R1xZzrHyoiVlgKpGiJ8cwIQLogRGQnWY7NwgQvRHCBmsl99bj57h7SWnd03"
            + "m6OCAYEwggF9MAkGA1UdEwQCMAAwHwYDVR0jBBgwFoAUScfc7QYUosdtnKbP11L9"
            + "aOXoBBQwcAYIKwYBBQUHAQEEZDBiMDMGCCsGAQUFBzAChidodHRwOi8vYy5zay5l"
            + "ZS9URVNUX0VJRC1RXzIwMjFFLmRlci5jcnQwKwYIKwYBBQUHMAGGH2h0dHA6Ly9h"
            + "aWEuZGVtby5zay5lZS9laWRxMjAyMWUweAYDVR0gBHEwbzAIBgYEAI96AQIwYwYJ"
            + "KwYBBAHOHxIBMFYwVAYIKwYBBQUHAgEWSGh0dHBzOi8vd3d3LnNraWRzb2x1dGlv"
            + "bnMuZXUvcmVzb3VyY2VzL2NlcnRpZmljYXRpb24tcHJhY3RpY2Utc3RhdGVtZW50"
            + "LzA0BgNVHR8ELTArMCmgJ6AlhiNodHRwOi8vYy5zay5lZS90ZXN0X2VpZC1xXzIw"
            + "MjFlLmNybDAdBgNVHQ4EFgQUj8KjnXvGQJCRYOd5LVfPku7QsZwwDgYDVR0PAQH_"
            + "BAQDAgeAMAoGCCqGSM49BAMCA2gAMGUCMQCocXWDbBnkM3WEyBdv9Vm0A1MNRv08"
            + "WrR192dRBcX42Kz5oiH0SdHRJv2ffeuEeSwCMEw2tSA3ClJv233Dl7rIYU_T6UG2"
            + "NQhvDD5FhnP0umZRmVfAUQ6eVcmU8AhFtNJjwg==";

    private static final String UNSIGNED_JWT_SAMPLE =
        "eyJ0eXAiOiJ2bmQuY2RvYzIuc2Vzc2lvbi10b2tlbi52MitzZC1qd3QiLCJhbGciOiJFUzI1NiJ9."
            + "eyJpc3MiOiJodHRwczovL2Nkb2MyLWF1dGgtc2VydmVyLmVlIiwiX3NkIjpbInQiXSwiX3NkX2FsZyI6InNoYS0yNTYifQ."
            + "AAAA~";

    private static final String INTERACTIONS_DIGEST = "BASE64DIGEST==";
    private static final String RP_CHALLENGE_BASE64 =
        Base64.getEncoder().encodeToString(new byte[64]);

    private static final String SAMPLE_SIGNATURE_VALUE =
        "Z1ntyPydVQ0TWFlphx5qbzeP102jTWRjsVFzEBwjuBjhXVnraDdW0vB1SzFc/eTSowkz3sN6";
    private static final String SAMPLE_ALGORITHM = "SHA256WithECEncryption";

    private static final String SIGNED_SD_JWT_RESULT = "SIGNED-SD-JWT-FROM-MOCK";

    @Mock
    private CreateSignedSdJwtForMid createSignedSdJwtForMid;

    @Mock
    private GetSessionTokenMaterial getSessionTokenMaterial;

    @Mock
    private MidAuthenticationResponseValidator responseValidator;

    @Mock
    private MobileIdConf mobileIdConf;

    @Mock
    private MidAuthenticationResult validatorResult;

    @InjectMocks
    private CreateMidSessionToken createMidSessionToken;

    private String midCertStandardBase64;

    @BeforeEach
    void setUp() {
        midCertStandardBase64 = toStandardBase64(MID_CERT_URL_SAFE);

        lenient().when(mobileIdConf.getMidAuthenticationResponseValidator())
            .thenReturn(responseValidator);
        lenient().when(mobileIdConf.isAuthenticationResponseValidationEnabled())
            .thenReturn(false);

        lenient().when(getSessionTokenMaterial.execute(any(GetSessionTokenMaterial.Request.class)))
            .thenReturn(new GetSessionTokenMaterial.Response(
                UNSIGNED_JWT_SAMPLE,
                INTERACTIONS_DIGEST,
                RP_CHALLENGE_BASE64
            ));

        lenient().when(createSignedSdJwtForMid.execute(UNSIGNED_JWT_SAMPLE))
            .thenReturn(SIGNED_SD_JWT_RESULT);
    }

    @Test
    void executeWithValidationEnabledReturnsSignedJwt() {
        when(mobileIdConf.isAuthenticationResponseValidationEnabled()).thenReturn(true);
        when(responseValidator.validate(any(MidAuthentication.class))).thenReturn(validatorResult);
        when(validatorResult.isValid()).thenReturn(true);
        when(validatorResult.getErrors()).thenReturn(List.of());

        String result = createMidSessionToken.execute(UUID.randomUUID(), midResponse());

        assertEquals(SIGNED_SD_JWT_RESULT, result);
        verify(createSignedSdJwtForMid).execute(UNSIGNED_JWT_SAMPLE);

        // Verify the validator was invoked exactly once with a non-null MidAuthentication
        // that includes the supplied signature value and algorithm.
        ArgumentCaptor<MidAuthentication> captor = ArgumentCaptor.forClass(MidAuthentication.class);
        verify(responseValidator).validate(captor.capture());

        MidAuthentication captured = captor.getValue();
        assertNotNull(captured);
        assertEquals(SAMPLE_SIGNATURE_VALUE, captured.getSignatureValueInBase64());
        assertEquals(SAMPLE_ALGORITHM, captured.getAlgorithmName());
        assertEquals("OK", captured.getResult());
    }

    @Test
    void executeWithValidationDisabledReturnsSignedJwt() {
        String result = createMidSessionToken.execute(UUID.randomUUID(), midResponse());

        assertEquals(SIGNED_SD_JWT_RESULT, result);
        verify(createSignedSdJwtForMid).execute(UNSIGNED_JWT_SAMPLE);

        // Verify the validator was never invoked
        verifyNoInteractions(responseValidator);
    }

    @Test
    void executeForwardsAuthUuid() {
        UUID authProcessUuid = UUID.randomUUID();
        createMidSessionToken.execute(authProcessUuid, midResponse());

        ArgumentCaptor<GetSessionTokenMaterial.Request> requestCaptor =
            ArgumentCaptor.forClass(GetSessionTokenMaterial.Request.class);
        verify(getSessionTokenMaterial).execute(requestCaptor.capture());

        assertEquals(authProcessUuid, requestCaptor.getValue().uuid());
    }

    @Test
    void executeNullResponseThrows() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> createMidSessionToken.execute(UUID.randomUUID(), null));

        assertEquals("MID session response missing", ex.getMessage());
        // None of the downstream collaborators should be touched
        verify(getSessionTokenMaterial, never()).execute(any());
        verify(createSignedSdJwtForMid, never()).execute(any());
    }

    @Test
    void executeNullSignatureThrows() {
        GetMidSession.Response responseWithoutSignature = new GetMidSession.Response(
            "COMPLETE",
            "OK",
            midCertStandardBase64,
            null // signature missing
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> createMidSessionToken.execute(UUID.randomUUID(), responseWithoutSignature));

        assertEquals("Signature was not present in the response", ex.getMessage());
        verify(responseValidator, never()).validate(any(MidAuthentication.class));
    }

    @Test
    void executeBlankSignatureThrows() {
        MidSessionSignature blankSignature = new MidSessionSignature();
        blankSignature.setValue("   ");
        blankSignature.setAlgorithm(SAMPLE_ALGORITHM);

        GetMidSession.Response response = new GetMidSession.Response(
            "COMPLETE",
            "OK",
            midCertStandardBase64,
            blankSignature
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> createMidSessionToken.execute(UUID.randomUUID(), response));

        assertEquals("Signature was not present in the response", ex.getMessage());
        verify(responseValidator, never()).validate(any(MidAuthentication.class));
    }

    @Test
    void executeInvalidValidationThrows() {
        when(mobileIdConf.isAuthenticationResponseValidationEnabled()).thenReturn(true);
        when(responseValidator.validate(any(MidAuthentication.class))).thenReturn(validatorResult);
        when(validatorResult.isValid()).thenReturn(false);
        when(validatorResult.getErrors()).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> createMidSessionToken.execute(UUID.randomUUID(), midResponse()));

        assertEquals("MID signature validation failed", ex.getMessage());
        verify(createSignedSdJwtForMid, never()).execute(any());
    }

    @Test
    void executeValidationErrorsThrows() {
        when(mobileIdConf.isAuthenticationResponseValidationEnabled()).thenReturn(true);
        when(responseValidator.validate(any(MidAuthentication.class))).thenReturn(validatorResult);
        when(validatorResult.isValid()).thenReturn(true);
        when(validatorResult.getErrors()).thenReturn(List.of(
            "Certificate is not trusted",
            "Hash mismatch"
        ));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> createMidSessionToken.execute(UUID.randomUUID(), midResponse()));

        assertEquals("MID signature validation failed", ex.getMessage());
        verify(createSignedSdJwtForMid, never()).execute(any());
    }

    private GetMidSession.Response midResponse() {
        MidSessionSignature signature = new MidSessionSignature();
        signature.setValue(SAMPLE_SIGNATURE_VALUE);
        signature.setAlgorithm(SAMPLE_ALGORITHM);

        return new GetMidSession.Response(
            "COMPLETE",
            "OK",
            midCertStandardBase64,
            signature
        );
    }

    private static String toStandardBase64(String urlSafe) {
        return urlSafe.replace('-', '+').replace('_', '/');
    }
}
