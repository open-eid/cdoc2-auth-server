package ee.cyber.cdoc2.server.app.usecase.common;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.authlete.sd.SDObjectDecoder;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.server.app.usecase.common.SessionToken.SessionTokenCreationParams;
import ee.cyber.cdoc2.server.app.usecase.startauth.SessionNonce;

import static ee.cyber.cdoc2.server.app.Constants.RP_V3_SIGNATURE_ALGORITHM_NAME;
import static ee.cyber.cdoc2.server.app.Constants.SESSION_TOKEN_JWT_TYP;
import static org.junit.jupiter.api.Assertions.*;

class SessionTokenTest {
    private static final String ETSI_IDENTIFIER = "etsi/PNOEE-48010010101";
    private static final URI URI_1 = URI.create("http://localhost/1");
    private static final String URI_1_NONCE = "123";
    private static final String URI_2_NONCE = "456";
    private static final URI URI_2 = URI.create("http://localhost/2");
    private static final String ISS = "test.example.com";
    private static final int EXPECTED_DISCLOSURES_COUNT = 3;
    private static final int EXPECTED_HEADER_SIZE = 2;
    private static final int EXPECTED_PAYLOAD_SIZE = 6;

    @Test
    void sdJwtHasCorrectDisclosures() {
        SDJWT sdjwt = createSessionToken();

        List<Disclosure> disclosures = sdjwt.getDisclosures();

        assertEquals(EXPECTED_DISCLOSURES_COUNT, disclosures.size());

        Optional<Disclosure> audArrayDisclosure = disclosures.stream()
            .filter(d -> "aud".equals(d.getClaimName()))
            .findFirst();

        assertTrue(audArrayDisclosure.isPresent());

        assertTrue(disclosures.stream().anyMatch(d -> d.getClaimValue().toString().equals(
                URI_1 + "/" + URI_1_NONCE
            )
        ));

        assertTrue(disclosures.stream().anyMatch(d -> d.getClaimValue().toString().equals(
                URI_2 + "/" + URI_2_NONCE
            )
        ));
    }

    @Test
    void sdJwtHasCorrectHeader() throws ParseException {
        SDJWT sdjwt = createSessionToken();

        Base64URL headerBase64 = new Base64URL(getHeaderPart(sdjwt.getCredentialJwt()));
        Map<String, Object> header = JSONObjectUtils.parse(headerBase64.decodeToString());

        assertEquals(EXPECTED_HEADER_SIZE, header.size());
        assertEquals(SESSION_TOKEN_JWT_TYP, header.get("typ"));
        assertEquals(RP_V3_SIGNATURE_ALGORITHM_NAME, header.get("alg"));
    }

    @Test
    void sdJwtHasCorrectPayload() throws ParseException {
        SDJWT sdjwt = createSessionToken();

        Base64URL payloadBase64 = new Base64URL(getPayloadPart(sdjwt.getCredentialJwt()));
        Map<String, Object> payload = JSONObjectUtils.parse(payloadBase64.decodeToString());

        assertEquals(EXPECTED_PAYLOAD_SIZE, payload.size());
        assertEquals(ETSI_IDENTIFIER, payload.get("sub"));
        assertEquals(ISS, payload.get("iss"));
        assertEquals("sha-256", payload.get("_sd_alg"));
        assertNotNull(payload.get("iat"));
        assertNotNull(payload.get("exp"));
        assertNotNull(payload.get("_sd"));
    }

    @Test
    void sdJwtIsDecodedCorrectly() throws ParseException {
        SDObjectDecoder decoder = new SDObjectDecoder();
        SDJWT sdjwt = createSessionToken();

        String credentialJwtWithMockSignature = sdjwt.getCredentialJwt() + ".ABC123";

        SignedJWT signedJWT = SignedJWT.parse(credentialJwtWithMockSignature);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        Map<String, Object> claimsMap = claimsSet.getClaims();

        Map<String, Object> decodedClaims = decoder.decode(claimsMap, sdjwt.getDisclosures());
        Object aud = decodedClaims.get("aud");

        assertInstanceOf(ArrayList.class, aud);

        ArrayList<?> audArray = (ArrayList<?>) aud;
        assertEquals(2, audArray.size());

        assertEquals(URI_1 + "/" + URI_1_NONCE, audArray.get(0));
        assertEquals(URI_2 + "/" + URI_2_NONCE, audArray.get(1));
    }

    private static SDJWT createSessionToken() {
        SessionTokenCreationParams params = new SessionTokenCreationParams(
            List.of(
                new SessionNonce.UriSessionNonce(URI_1, URI_1_NONCE),
                new SessionNonce.UriSessionNonce(URI_2, URI_2_NONCE)
            ),
            new EtsiIdentifier(ETSI_IDENTIFIER),
            ISS
        );
        return SessionToken.unsignedSdJwtWithAllDisclosures(params);
    }

    private String getHeaderPart(String jwt) {
        return jwt.substring(0, jwt.indexOf("."));
    }

    private String getPayloadPart(String jwt) {
        return jwt.substring(jwt.indexOf(".") + 1);
    }
}
