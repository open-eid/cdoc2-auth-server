package ee.cyber.cdoc2.server.app.usecase.startauth;

import ee.sk.smartid.exception.permanent.SmartIdClientException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.auth.EtsiIdentifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartAuthImplTest {
    private static final String VALID_ETSI = "etsi/PNOEE-50101010009";
    private static final String VALID_PHONE_NR = "+37269930366";

    @Mock
    private StartSidAuth startSidAuth;
    @Mock
    private StartMidAuth startMidAuth;
    @InjectMocks
    private StartAuthImpl startAuth;

    private static final int EXPECTED_RP_CHALLENGE_LENGTH = 64;

    @Test
    void createRpChallengeBytesReturnsNonNullArrayOfCorrectLength() {
        byte[] result = StartAuthImpl.createRpChallengeBytes();

        assertNotNull(result);
        assertEquals(EXPECTED_RP_CHALLENGE_LENGTH, result.length);
    }

    @Test
    void createRpChallengeBytesProducesDistinctValuesOnEachCall() {
        byte[] first = StartAuthImpl.createRpChallengeBytes();
        byte[] second = StartAuthImpl.createRpChallengeBytes();

        assertNotEquals(first, second);
    }

    @ParameterizedTest
    @MethodSource("knownSidVectors")
    void startSidAuthCalculatesCorrectVerificationCode(byte[] input, String expected) {
        Function<byte[], String> vcFunction = retrieveVerificationCodeFunctionForSid();

        String verificationCode = vcFunction.apply(input);

        assertEquals(expected, verificationCode);
    }

    @Test
    void startSidAuthVerificationCodeCodeCalculatorThrowsSidExceptionForNullInput() {
        Function<byte[], String> vcCodeFunction = retrieveVerificationCodeFunctionForSid();

        SmartIdClientException ex = assertThrows(
            SmartIdClientException.class,
            () -> vcCodeFunction.apply(null)
        );
        assertEquals("Parameter 'data' cannot be empty", ex.getMessage());
    }

    @Test
    void startSidAuthVerificationCodeCodeCalculatorThrowsSidExceptionForEmptyInput() {
        Function<byte[], String> vcFunction = retrieveVerificationCodeFunctionForSid();

        SmartIdClientException ex = assertThrows(
            SmartIdClientException.class,
            () -> vcFunction.apply(new byte[0])
        );
        assertEquals("Parameter 'data' cannot be empty", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("knownMidVectors")
    void startMidAuthCalculatesCorrectVerificationCode(byte[] input, String expected) {
        Function<byte[], String> vcFunction = retrieveVerificationCodeFunctionForMid();

        String verificationCode = vcFunction.apply(input);

        assertEquals(expected, verificationCode);
    }

    static Stream<Arguments> knownSidVectors() {
        return Stream.of(
            // sha256({0x01})[-2:] = 0x45 0x9a → 17818 → "7818"
            Arguments.of(new byte[]{0x01}, "7818"),
            Arguments.of(new byte[]{0x00}, "0989"),
            // sha256("hello")[-2:] = 0x98 0x24 → 38948 → "8948"
            Arguments.of("hello".getBytes(StandardCharsets.UTF_8), "8948"),
            // sha256(bytes 0-63)[-2:] = 0x11 0x08 → 4360 → "4360"
            Arguments.of(zeroToSixtyThree(), "4360"),
            // sha256({0x42, 0x13})[-2:] = ... → "9055"
            Arguments.of(new byte[]{0x42, 0x13}, "9055")
        );
    }

    // MID VC calculation has a minimum input size of 20 bytes, shorter input returns '0000'
    // this includes null input and empty array
    static Stream<Arguments> knownMidVectors() {
        return Stream.of(
            // sha256({0x01})[-2:] = 0x45 0x9a → 0000 (belows minimum length)
            Arguments.of(new byte[]{0x01}, "0000"),
            Arguments.of(repeat((byte) 0xFF, 19), "0000"),
            Arguments.of(repeat((byte) 0xFF, 20), "8191"),
            Arguments.of(repeat((byte) 0x34, 20), "1716"),
            Arguments.of(zeroToSixtyThree(), "0063"),
            Arguments.of(repeat((byte) 0x12, 32), "0530"),
            Arguments.of(null, "0000"),
            Arguments.of(new byte[0], "0000")
        );
    }

    private Function<byte[], String> retrieveVerificationCodeFunctionForSid() {
        ArgumentCaptor<Function<byte[], String>> captor = ArgumentCaptor.forClass(Function.class);

        startAuth.execute(new StartAuth.Request(
            VALID_ETSI, null, Language.EN
        ));

        verify(startSidAuth).execute(any(UUID.class), any(EtsiIdentifier.class),
            any(Language.class), captor.capture());

        return captor.getValue();
    }

    private Function<byte[], String> retrieveVerificationCodeFunctionForMid() {
        ArgumentCaptor<Function<byte[], String>> captor = ArgumentCaptor.forClass(Function.class);

        startAuth.execute(new StartAuth.Request(
            VALID_ETSI, VALID_PHONE_NR, Language.EN
        ));

        verify(startMidAuth).execute(any(UUID.class), any(EtsiIdentifier.class), anyString(),
            any(Language.class), captor.capture());

        return captor.getValue();
    }

    private static byte[] zeroToSixtyThree() {
        byte[] zeroToSixtyThree = new byte[64];
        for (int i = 0; i < 64; i++) {
            zeroToSixtyThree[i] = (byte) i;
        }
        return zeroToSixtyThree;
    }

    private static byte[] repeat(byte value, int length) {
        byte[] arr = new byte[length];
        Arrays.fill(arr, value);
        return arr;
    }
}
