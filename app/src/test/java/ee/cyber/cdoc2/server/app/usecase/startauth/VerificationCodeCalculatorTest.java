package ee.cyber.cdoc2.server.app.usecase.startauth;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ee.sk.smartid.VerificationCodeCalculator;
import ee.sk.smartid.exception.permanent.SmartIdClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationCodeCalculatorTest {

    @ParameterizedTest
    @MethodSource("knownVectors")
    void producesExpectedFourDigitCode(byte[] input, String expected) {
        String actual = VerificationCodeCalculator.calculate(input);

        assertEquals(expected, actual);
        assertTrue(actual.matches("\\d{4}"), "Expected 4-digit code but got: " + actual);
    }

    static Stream<Arguments> knownVectors() {
        byte[] zeroToSixtyThree = new byte[64];
        for (int i = 0; i < 64; i++) {
            zeroToSixtyThree[i] = (byte) i;
        }
        return Stream.of(
            // sha256({0x01})[-2:] = 0x45 0x9a → 17818 → "7818"
            Arguments.of(new byte[]{0x01}, "7818"),
            // sha256({0x00})[-2:] = 0xa0 0x1d → 40989 → "0989" (tests leading zero)
            Arguments.of(new byte[]{0x00}, "0989"),
            // sha256("hello")[-2:] = 0x98 0x24 → 38948 → "8948"
            Arguments.of("hello".getBytes(StandardCharsets.UTF_8), "8948"),
            // sha256(bytes 0-63)[-2:] = 0x11 0x08 → 4360 → "4360"
            Arguments.of(zeroToSixtyThree, "4360"),
            // sha256({0x42, 0x13})[-2:] = ... → "9055"
            Arguments.of(new byte[]{0x42, 0x13}, "9055")
        );
    }

    @Test
    void throwsForNullInput() {
        SmartIdClientException ex = assertThrows(
            SmartIdClientException.class,
            () -> VerificationCodeCalculator.calculate(null)
        );
        assertEquals("Parameter 'data' cannot be empty", ex.getMessage());
    }

    @Test
    void throwsForEmptyInput() {
        SmartIdClientException ex = assertThrows(
            SmartIdClientException.class,
            () -> VerificationCodeCalculator.calculate(new byte[0])
        );
        assertEquals("Parameter 'data' cannot be empty", ex.getMessage());
    }
}
