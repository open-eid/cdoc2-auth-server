package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.server.app.conf.DisplayTextConf;
import ee.cyber.cdoc2.server.app.conf.SessionTokenConf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class StartSidAuthTest {
    private static final EtsiIdentifier VALID_ETSI = new EtsiIdentifier("etsi/PNOEE-50101010009");

    @Mock
    private StoreAuthProcess storeAuthProcess;
    @Mock
    private SessionNonce sessionNonce;
    @Mock
    private SidAuthenticate sidAuthenticate;
    @Mock
    private DisplayTextConf displayTextConf;
    @Mock
    private SessionTokenConf sessionTokenConf;

    @InjectMocks
    private StartSidAuth startSidAuth;

    @BeforeEach
    void setUp() {
        Mockito.when(sessionNonce.collectSessionNonces())
            .thenReturn(List.of());
        Mockito.when(displayTextConf.getDisplayText(any(), any()))
            .thenReturn("DISPLAY_TEXT");
    }

    @Test
    void verificationCodeCalculationIsCalledWithCorrectByteArrayLength() {
        String verificationCode = startSidAuth.execute(
            UUID.randomUUID(),
            VALID_ETSI,
            Language.EN,
            this::mockVerificationCodeFunctionRpChallengeLength
        );

        assertEquals("64", verificationCode);
    }

    private String mockVerificationCodeFunctionRpChallengeLength(byte[] rpChallenge) {
        return String.valueOf(rpChallenge.length);
    }
}
