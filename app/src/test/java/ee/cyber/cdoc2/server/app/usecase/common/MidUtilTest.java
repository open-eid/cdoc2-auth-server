package ee.cyber.cdoc2.server.app.usecase.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidHashType;

import static org.junit.jupiter.api.Assertions.*;

class MidUtilTest {

    @Test
    void createAuthenticationHashProducesSha256OfInput() throws NoSuchAlgorithmException {
        byte[] input = "some-rp-challenge".getBytes(StandardCharsets.UTF_8);

        MidAuthenticationHashToSign hash = MidUtil.createAuthenticationHash(input);

        byte[] expected = MessageDigest.getInstance("SHA-256").digest(input);
        assertArrayEquals(expected, hash.getHash());
    }

    @Test
    void createAuthenticationHashUsesSha256HashType() {
        byte[] input = "some-rp-challenge".getBytes(StandardCharsets.UTF_8);

        MidAuthenticationHashToSign hash = MidUtil.createAuthenticationHash(input);

        assertEquals(MidHashType.SHA256, hash.getHashType());
    }
}
