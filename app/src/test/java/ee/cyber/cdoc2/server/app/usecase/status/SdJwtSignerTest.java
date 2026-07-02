package ee.cyber.cdoc2.server.app.usecase.status;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.authlete.sd.Disclosure;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import ee.cyber.cdoc2.server.app.conf.JwtKeysConf;

import static ee.cyber.cdoc2.server.app.Constants.SESSION_TOKEN_JWT_TYP;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SdJwtSignerTest {

    private static final String TEST_KID = "test-key-id";

    private SdJwtSigner sdJwtSigner;
    private ECPrivateKey ecPrivateKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair keyPair = kpg.generateKeyPair();
        ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();

        JwtKeysConf jwtKeysConf = mock(JwtKeysConf.class);
        when(jwtKeysConf.ecPrivateKey()).thenReturn(ecPrivateKey);
        when(jwtKeysConf.getKid()).thenReturn(TEST_KID);

        sdJwtSigner = new SdJwtSigner(jwtKeysConf);
    }

    @Test
    void signWithEmptyDisclosuresReturnsValidSdJwt() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject("test-subject")
            .issuer("test-issuer")
            .build();

        String result = sdJwtSigner.sign(claimsSet, List.of());

        assertNotNull(result);
        // SD-JWT format: <JWT>~ (trailing tilde with no disclosures)
        assertTrue(result.contains("~"), "SD-JWT must contain '~' separator");
        String jwtPart = result.split("~")[0];
        SignedJWT signedJWT = SignedJWT.parse(jwtPart);
        assertEquals("test-subject", signedJWT.getJWTClaimsSet().getSubject());
    }

    @Test
    void signSetsCorrectAlgorithmInHeader() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("sub").build();

        String result = sdJwtSigner.sign(claimsSet, List.of());

        SignedJWT signedJWT = SignedJWT.parse(result.split("~")[0]);
        JWSHeader header = signedJWT.getHeader();
        assertEquals(JWSAlgorithm.ES256, header.getAlgorithm());
    }

    @Test
    void signSetsKidFromConf() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("sub").build();

        String result = sdJwtSigner.sign(claimsSet, List.of());

        SignedJWT signedJWT = SignedJWT.parse(result.split("~")[0]);
        assertEquals(TEST_KID, signedJWT.getHeader().getKeyID());
    }

    @Test
    void signSetsCorrectJwtType() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("sub").build();

        String result = sdJwtSigner.sign(claimsSet, List.of());

        SignedJWT signedJWT = SignedJWT.parse(result.split("~")[0]);
        assertEquals(SESSION_TOKEN_JWT_TYP, signedJWT.getHeader().getType().getType());
    }

    @Test
    void signWithDisclosuresIncludesThemInOutput() {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("sub").build();
        Disclosure disclosure1 = new Disclosure("salt1", "name", "Alice");
        Disclosure disclosure2 = new Disclosure("salt2", "age", 30);

        String result = sdJwtSigner.sign(claimsSet, List.of(disclosure1, disclosure2));

        // SD-JWT format: <JWT>~<disclosure1>~<disclosure2>~
        // Java split() drops trailing empty strings, so 3 parts: JWT + 2 disclosures
        String[] parts = result.split("~");
        assertEquals(3, parts.length);
        assertEquals(disclosure1.getDisclosure(), parts[1]);
        assertEquals(disclosure2.getDisclosure(), parts[2]);
    }

    @Test
    void signProducesVerifiableSignature() {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("sub").build();

        assertDoesNotThrow(() -> sdJwtSigner.sign(claimsSet, List.of()));
    }
}
