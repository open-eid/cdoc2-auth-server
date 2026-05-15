package ee.cyber.cdoc2.server.app.usecase.common;

import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidHashToSign;
import ee.sk.mid.MidHashType;

public final class MidUtil {
    private MidUtil() {
        // Utility class
    }

    public static MidAuthenticationHashToSign createAuthenticationHash(byte[] rpChallenge) {
        MidHashToSign hashToSign = MidHashToSign.newBuilder()
            .withDataToHash(rpChallenge)
            .withHashType(MidHashType.SHA256)
            .build();

        byte[] hashBytes = hashToSign.getHash();

        return MidAuthenticationHashToSign.newBuilder()
            .withHash(hashBytes)
            .withHashType(MidHashType.SHA256)
            .build();
    }
}
