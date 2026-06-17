package ee.cyber.cdoc2.server.app.usecase.startauth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StartAuthImplTest {

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
}
