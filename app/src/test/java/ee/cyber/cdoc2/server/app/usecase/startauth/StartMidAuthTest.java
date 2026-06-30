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

@ExtendWith(MockitoExtension.class)
class StartMidAuthTest {
    private static final EtsiIdentifier VALID_ETSI = new EtsiIdentifier("etsi/PNOEE-50101010009");
    private static final String VALID_PHONE_NR = "+37269930366";

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
    }

    @Test
    void verificationCodeCalculationIsCalledWithCorrectByteArraySize() {
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
}
