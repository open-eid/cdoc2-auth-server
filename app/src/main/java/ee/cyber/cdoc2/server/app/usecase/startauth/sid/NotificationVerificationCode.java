package ee.cyber.cdoc2.server.app.usecase.startauth.sid;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class NotificationVerificationCode {
    private static final int RP_CHALLENGE_SIZE_MIN = 32;
    private static final int RP_CHALLENGE_SIZE_MAX = 64;

    private NotificationVerificationCode() {
        // utility class
    }

    public static String create(byte[] rpChallengeBytes) {
        if (rpChallengeBytes.length > RP_CHALLENGE_SIZE_MAX
            || rpChallengeBytes.length < RP_CHALLENGE_SIZE_MIN) {
            throw new RuntimeException("Illegal size for rpChallenge");
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(rpChallengeBytes);
            byte[] sha256Hash = md.digest();

            BigInteger lastTwoBytesBitmask = new BigInteger("65535");

            BigInteger result = new BigInteger(sha256Hash)
                .and(lastTwoBytesBitmask)
                .mod(new BigInteger("10000"));

            return String.format("%04d", result);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
