package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.server.app.conf.DisplayTextConf;
import ee.cyber.cdoc2.server.app.exception.InputValidationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Covers input-validation failures in {@link StartMidAuth#execute}, which short-circuit
 * before {@link SessionNonce}, {@link DisplayTextConf} or {@link MidAuthenticate} are ever
 * called - so unlike {@link StartMidAuthTest}, no stubbing is needed here.
 */
@ExtendWith(MockitoExtension.class)
class StartMidAuthValidationTest {

    private static final EtsiIdentifier VALID_ETSI = new EtsiIdentifier("etsi/PNOEE-50101010009");
    private static final EtsiIdentifier INVALID_ETSI = new EtsiIdentifier("etsi/PNOEE-50101010008");
    private static final String VALID_PHONE_NR = "+37269930366";

    @Mock
    private StoreAuthProcess storeAuthProcess;
    @Mock
    private MidAuthenticate midAuthenticate;

    @InjectMocks
    private StartMidAuth startMidAuth;

    @Test
    void invalidPhoneFormatThrowsInputValidationException() {
        assertThrows(InputValidationException.class,
            () -> startMidAuth.execute(UUID.randomUUID(), VALID_ETSI, "12345", Language.EN, h -> "ignored"));

        verify(midAuthenticate, never()).execute(any());
        verify(storeAuthProcess, never()).execute(any());
    }

    @Test
    void invalidNationalIdentityNumberThrowsInputValidationException() {
        assertThrows(InputValidationException.class,
            () -> startMidAuth.execute(
                UUID.randomUUID(), INVALID_ETSI, VALID_PHONE_NR, Language.EN, h -> "ignored"
            ));

        verify(midAuthenticate, never()).execute(any());
        verify(storeAuthProcess, never()).execute(any());
    }

    @Test
    void nullPhoneThrowsInputValidationOrNpe() {
        assertThrows(InputValidationException.class,
            () -> startMidAuth.execute(UUID.randomUUID(), VALID_ETSI, null, Language.EN, h -> "ignored"));

        verify(midAuthenticate, never()).execute(any());
        verify(storeAuthProcess, never()).execute(any());
    }
}
