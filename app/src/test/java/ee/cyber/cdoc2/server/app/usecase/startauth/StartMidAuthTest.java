package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.server.app.conf.DisplayTextConf;
import ee.cyber.cdoc2.server.app.conf.SessionTokenConf;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartMidAuthTest {
    private static final EtsiIdentifier VALID_ETSI = new EtsiIdentifier("etsi/PNOEE-50101010009");
    private static final String VALID_PHONE_NR = "+37269930366";
    private static final UUID MID_SESSION_ID = UUID.randomUUID();

    @Mock
    private StoreAuthProcess storeAuthProcess;
    @Mock
    private SessionNonce sessionNonce;
    @Mock
    private MidAuthenticate midAuthenticate;
    @Mock
    private DisplayTextConf displayTextConf;
    @Mock
    private SessionTokenConf sessionTokenConf;

    @InjectMocks
    private StartMidAuth startMidAuth;

    @BeforeEach
    void setUp() {
        Mockito.when(sessionNonce.collectSessionNonces())
            .thenReturn(List.of());
        Mockito.when(midAuthenticate.execute(any()))
            .thenReturn(MID_SESSION_ID);
    }

    @Test
    void verificationCodeCalculationIsCalledWithCorrectByteArrayLength() {
        String verificationCode = startMidAuth.execute(
            UUID.randomUUID(),
            VALID_ETSI,
            VALID_PHONE_NR,
            Language.EN,
            this::mockVerificationCodeFunctionAuthHashLength
        );

        assertEquals("32", verificationCode);
    }

    private String mockVerificationCodeFunctionAuthHashLength(byte[] authHash) {
        return String.valueOf(authHash.length);
    }

    @Test
    void storeAuthProcessWithTypeMID() {
        UUID authProcessUuid = UUID.randomUUID();

        startMidAuth.execute(authProcessUuid, VALID_ETSI, VALID_PHONE_NR, Language.EN, h -> "ignored");

        ArgumentCaptor<StoreAuthProcess.Request> captor = ArgumentCaptor.forClass(StoreAuthProcess.Request.class);
        verify(storeAuthProcess).execute(captor.capture());
        StoreAuthProcess.Request stored = captor.getValue();

        assertEquals(authProcessUuid, stored.authUuid());
        assertEquals(AuthProcessType.MID, stored.type());
        assertNotNull(stored.midSidSessionId());
        assertNotNull(stored.unsignedSdJwt());
        assertNotNull(stored.rpChallenge());
    }

    @Test
    void storeRpChallengeAsBase64() {
        startMidAuth.execute(UUID.randomUUID(), VALID_ETSI, VALID_PHONE_NR, Language.EN, h -> "ignored");

        ArgumentCaptor<StoreAuthProcess.Request> captor = ArgumentCaptor.forClass(StoreAuthProcess.Request.class);
        verify(storeAuthProcess).execute(captor.capture());

        byte[] decodedRpChallenge = Base64.getDecoder().decode(captor.getValue().rpChallenge());

        assertEquals(64, decodedRpChallenge.length);
    }

    @Test
    void sha256RpChallengeHashToMidAuthenticate() throws NoSuchAlgorithmException {
        startMidAuth.execute(UUID.randomUUID(), VALID_ETSI, VALID_PHONE_NR, Language.EN, h -> "ignored");

        ArgumentCaptor<StoreAuthProcess.Request> storeCaptor =
            ArgumentCaptor.forClass(StoreAuthProcess.Request.class);
        verify(storeAuthProcess).execute(storeCaptor.capture());
        byte[] rpChallenge = Base64.getDecoder().decode(storeCaptor.getValue().rpChallenge());

        ArgumentCaptor<MidAuthenticate.Request> midCaptor = ArgumentCaptor.forClass(MidAuthenticate.Request.class);
        verify(midAuthenticate).execute(midCaptor.capture());
        byte[] actualHash = midCaptor.getValue().authenticationHash().getHash();

        byte[] expectedHash = MessageDigest.getInstance("SHA-256").digest(rpChallenge);

        assertArrayEquals(expectedHash, actualHash);
    }

    @Test
    void displayTextContainsEtsiSemanticsIdentifier() {
        String cdoc2DisplayText = "CDOC2 authentication request: " + VALID_ETSI.getSemanticsIdentifier();
        Mockito.when(displayTextConf.getDisplayText(Language.EN, VALID_ETSI.getSemanticsIdentifier()))
            .thenReturn(cdoc2DisplayText);

        startMidAuth.execute(UUID.randomUUID(), VALID_ETSI, VALID_PHONE_NR, Language.EN, h -> "ignored");

        ArgumentCaptor<MidAuthenticate.Request> midCaptor = ArgumentCaptor.forClass(MidAuthenticate.Request.class);
        verify(midAuthenticate).execute(midCaptor.capture());
        String displayText = midCaptor.getValue().displayText();

        assertTrue(displayText.startsWith("CDOC2"));
        assertTrue(displayText.contains(VALID_ETSI.getSemanticsIdentifier()));
    }

    @Test
    void returnVerificationCodeFromAuthenticationHash() {
        String verificationCode = startMidAuth.execute(
            UUID.randomUUID(),
            VALID_ETSI,
            VALID_PHONE_NR,
            Language.EN,
            authHash -> "VC-" + authHash.length
        );

        ArgumentCaptor<MidAuthenticate.Request> midCaptor = ArgumentCaptor.forClass(MidAuthenticate.Request.class);
        verify(midAuthenticate).execute(midCaptor.capture());
        byte[] authenticationHash = midCaptor.getValue().authenticationHash().getHash();

        assertEquals("VC-" + authenticationHash.length, verificationCode);
    }

    @Test
    void forwardValidPhoneAndIdentityToMidAuthenticate() {
        startMidAuth.execute(UUID.randomUUID(), VALID_ETSI, VALID_PHONE_NR, Language.EN, h -> "ignored");

        ArgumentCaptor<MidAuthenticate.Request> midCaptor = ArgumentCaptor.forClass(MidAuthenticate.Request.class);
        verify(midAuthenticate).execute(midCaptor.capture());
        MidAuthenticate.Request request = midCaptor.getValue();

        assertEquals(VALID_PHONE_NR, request.phoneNumber());
        assertEquals(VALID_ETSI.getIdentifier(), request.nationalIdentityNumber());
    }
}
