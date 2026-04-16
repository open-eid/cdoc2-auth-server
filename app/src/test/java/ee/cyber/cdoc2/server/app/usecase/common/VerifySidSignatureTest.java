package ee.cyber.cdoc2.server.app.usecase.common;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.text.ParseException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.authlete.sd.SDJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerifySidSignatureTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // SESSION_TOKEN_BASE64URL, SID_SIGNING_CERTIFICATE are from auth/status endpoint response
    private static final String SESSION_TOKEN_BASE64URL =
        "eyJraWQiOiJlYy1rZXktMjAyNiIsInR5cCI6InZuZC5jZG9jMi5zZXNzaW9uLXRva2VuLnYyK3NkLWp3dCIsImFsZyI6IkVTMjU2In0.eyJycENoYWxsZW5nZSI6InA0WjFlT0hqM0J1RlJDUkx6b0xLeE8zZVRqUDZUTmVQTVZacThFZUdiSGlQM0d1UDZ1Q0Q4d2VETkE3L2hicG5Cb0hpMnUzTnRyTUxlSThFRlgrNnN3PT0iLCJfc2QiOlsiMklzNnFHa0pUcDRzcjBkWi1jOFlfeFc5akJrMUJjS2xWTEZEWDEzLXVrWSJdLCJzdWIiOiJldHNpL1BOT0VFLTQwNTA0MDQwMDAxLURFTTAtUSIsImludGVyYWN0aW9uc0RpZ2VzdCI6ImNjek4wL2ZUS2FvRmdmQVllTnhTTFJtWFFvVWcybEYxb0N6VVRYTkpJblk9Iiwic2lnbmF0dXJlIjp7InZhbHVlIjoiUFFuZ3JQeWREU0lPUk5hMGhDOGF1VXlocElnQ1hMcUV1cnRIWTdRSGpjSnVyTWQwaEE4eE1BU2ZFV3JjWVFUNkcvcGYzY09kODV1TDJVTVJjeE9aUHF3K2NYc29tc2N0Z1ZsSFVNU2luNy9sLzBzSVBSVzAyQzhUM2dJbWx3WHc1cW16N1ZOOVVyNjlqQjR3WDFlN0ZsYlNoUVJKL0k2OVlxWUtpQXdobm1HWVpGQnhSZGxUc0xvNUs2c2czcTdZT0M4M3M5T3JmNkpXVjVhZDRYeUwxdURNME9JMDZjdDVXeEVRUm90ZFVzTlRTaFVNYTZoUXVOQzEyQnQrV2lMU0c5a1ZEdmNrOFQwS2ZlRmw4dlgxTDQzcHNGemZKT3dUS1VFRUJheEIySzRyVlRPZWU3MkZUeEJQRGZGczdqZHRhK0VLTSsvZm1KWXhsZ3NuN1VhMUtNZzZHWEtSV3lkbHJyOGZMVGFaaEc0MmtnTEVVL2RhOFB4MUdxWmRmblZuUklRa24zUWRvV0RvZzhMWVJOZnFhM1BrMHdUMWdBMllYMWFqQmhEc1EzNmFvN1d0WWdiS3hlcjZ3NDViMlA4bWpvNG1FbFVEM0xwUW1SYVRXZGIvQkVxVCsyanlFbHl6Nm9UZUUvOU1jZkdmSGtlOWhPQytGWmJUNzNRcUQzUGwwZ1lVMEU0SFNTSEg3ZDBsVldqT2c1ZjM3bDlhSDluWUpUYkFBQWhHQ3dibUkydEFHeXh0SWxETFp3UjZaRCtJUlg3QUhzaUtqWDl1MUZHSEhjTHd4cldtd0NHZ2U3VTROSzZVM2QzYWd4bnZxZmJLRmhSZ2ZtcXNML2hXdnhZT2N2Z3lHcnh2MVZTa1BwR215Q2dhVmoyeFpETUlrcTEvTEEyZEJmeXQwancvbHFPMnN5Wno3K0hHTG5sUjRsT3R1WVlRT3hxcWk1Q2xaSk56ZkMzeDBzWFc4UFUwYTc3cHZHS1MwbEEzTllMUUtxYlVkUlJZLzJvZTNpK2NpOUpmTnRXWFd6TWQwY25kSHNFQ29pS1ltZ3M1a0JCM2gzbDl3ZXZ0VFNXWmpYRDRQU0pYRTNpdjVlNGRENTNTRDhHYW5FUmYvRGxHM1R2ek1FSncwODh1Nm84ZWZUcjVaQXg3eVJwc3dxQUtTVVVMRzFKZFVtSkhpNUVyQlA2U0Y1c0dZM3hZcTNKa285UnlraC9EYzlyODZjUm5tbi9jdXB0T0ZqVHpyRHNlcWJJaWdmdkNOMyt0dEtCdjNhRXBkekJ5UklEelNQQmZVOWVhUWJYdkQ0V0Nsd0djc1ZRb0ZLZ0JQa2VIbERoakVuNVRxa09BN2ppMHpNSDlFMFM0bldWWSIsInNlcnZlclJhbmRvbSI6IlY0d1h1L1l3UTNUMVgxOHExajhOTmxTcyIsInVzZXJDaGFsbGVuZ2UiOiIxR3ZMdU45U0Jvd1l4ajFKd3NwQ2VFeXlsSUItd0FnNDZZTzluQzhXVzdNIiwic2lnbmF0dXJlQWxnb3JpdGhtIjoicnNhc3NhLXBzcyIsImZsb3dUeXBlIjoiTm90aWZpY2F0aW9uIiwic2lnbmF0dXJlQWxnb3JpdGhtUGFyYW1ldGVycyI6eyJoYXNoQWxnb3JpdGhtIjoiU0hBLTI1NiIsIm1hc2tHZW5BbGdvcml0aG0iOnsiYWxnb3JpdGhtIjoiaWQtbWdmMSIsInBhcmFtZXRlcnMiOnsiaGFzaEFsZ29yaXRobSI6IlNIQS0yNTYifX0sInNhbHRMZW5ndGgiOjMyLCJ0cmFpbGVyRmllbGQiOiIweGJjIn19LCJfc2RfYWxnIjoic2hhLTI1NiIsImlzcyI6Imh0dHBzOi8vY2RvYzItYXV0aC1zZXJ2ZXIuZWUiLCJzaWduYXR1cmVQcm90b2NvbCI6IlJTQVNTQS1QU1MrQUNTUF9WMiIsImV4cCI6MTc3NjQxODU4OCwiaWF0IjoxNzc2MzMyMTg4LCJpbnRlcmFjdGlvblR5cGVVc2VkIjoiY29uZmlybWF0aW9uTWVzc2FnZUFuZFZlcmlmaWNhdGlvbkNvZGVDaG9pY2UifQ.RnaBhdnk-m0j6sm4QurxkhOkerDl0GiWuBwpdBTP5Dg9zbP1ejS5bqf-FXqLwDKU1eo_eeBNBLLzrve5Q5aDLw~WyJfU1ZvNnRCaXladUlBNUx5RDVXM3d3IiwiYXVkIixbeyIuLi4iOiJIaFAzMmxRU0hOV2JmN0dKWlJQYVV5RDExX2ZuaEo4SEpvUUVUUWdJNzFnIn0seyIuLi4iOiJSNHgwb3hPUE1qbnRMdW4xSzlxM3VWUHh0UEEwdFRkQ3FYNkFlMFAxZC1FIn1dXQ~WyJzSm5fZjgwZzRkaWYyWml6Y25qYy1RIiwiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3Nlc3Npb25fbm9uY2VfMS8xMjM0NTY3ODkwOTg3NjU0MzIxIl0~WyJWZERhS3REMmF2STU1TEpMbFNsZEZnIiwiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3Nlc3Npb25fbm9uY2VfMi85ODc2NTQzMjEyMzQ1Njc4OSJd~";
    private static final String SID_SIGNING_CERTIFICATE =
        "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";

    // EXPECTED_INTERACTIONS_DIGEST_BASE64, EXPECTED_RP_CHALLENGE_BASE64 captured with debugger
    // as generated by service
    private static final String EXPECTED_INTERACTIONS_DIGEST_BASE64 = "cczN0/fTKaoFgfAYeNxSLRmXQoUg2lF1oCzUTXNJInY=";
    private static final String EXPECTED_RP_CHALLENGE_BASE64 = "p4Z1eOHj3BuFRCRLzoLKxO3eTjP6TNePMVZq8EeGbHiP3GuP6uCD8weDNA7/hbpnBoHi2u3NtrMLeI8EFX+6sw==";

    // values as returned by SID s/ession response
    private static final String EXPECTED_SID_SIGNATURE_BASE64 = "PQngrPydDSIORNa0hC8auUyhpIgCXLqEurtHY7QHjcJurMd0hA8xMASfEWrcYQT6G/pf3cOd85uL2UMRcxOZPqw+cXsomsctgVlHUMSin7/l/0sIPRW02C8T3gImlwXw5qmz7VN9Ur69jB4wX1e7FlbShQRJ/I69YqYKiAwhnmGYZFBxRdlTsLo5K6sg3q7YOC83s9Orf6JWV5ad4XyL1uDM0OI06ct5WxEQRotdUsNTShUMa6hQuNC12Bt+WiLSG9kVDvck8T0KfeFl8vX1L43psFzfJOwTKUEEBaxB2K4rVTOee72FTxBPDfFs7jdta+EKM+/fmJYxlgsn7Ua1KMg6GXKRWydlrr8fLTaZhG42kgLEU/da8Px1GqZdfnVnRIQkn3QdoWDog8LYRNfqa3Pk0wT1gA2YX1ajBhDsQ36ao7WtYgbKxer6w45b2P8mjo4mElUD3LpQmRaTWdb/BEqT+2jyElyz6oTeE/9McfGfHke9hOC+FZbT73QqD3Pl0gYU0E4HSSHH7d0lVWjOg5f37l9aH9nYJTbAAAhGCwbmI2tAGyxtIlDLZwR6ZD+IRX7AHsiKjX9u1FGHHcLwxrWmwCGge7U4NK6U3d3agxnvqfbKFhRgfmqsL/hWvxYOcvgyGrxv1VSkPpGmyCgaVj2xZDMIkq1/LA2dBfyt0jw/lqO2syZz7+HGLnlR4lOtuYYQOxqqi5ClZJNzfC3x0sXW8PU0a77pvGKS0lA3NYLQKqbUdRRY/2oe3i+ci9JfNtWXWzMd0cndHsECoiKYmgs5kBB3h3l9wevtTSWZjXD4PSJXE3iv5e4dD53SD8GanERf/DlG3TvzMEJw088u6o8efTr5ZAx7yRpswqAKSUULG1JdUmJHi5ErBP6SF5sGY3xYq3Jko9Rykh/Dc9r86cRnmn/cuptOFjTzrDseqbIigfvCN3+ttKBv3aEpdzByRIDzSPBfU9eaQbXvD4WClwGcsVQoFKgBPkeHlDhjEn5TqkOA7ji0zMH9E0S4nWVY";
    private static final String EXPECTED_SERVER_RANDOM_BASE64 = "V4wXu/YwQ3T1X18q1j8NNlSs";
    private static final String EXPECTED_USER_CHALLENGE_BASE64URL = "1GvLuN9SBowYxj1JwspCeEyylIB-wAg46YO9nC8WW7M";
    private static final String EXPECTED_INTERACTION_TYPE_USED =
        "confirmationMessageAndVerificationCodeChoice";

    @Test
    void verifyRpV3Signature() throws ParseException, NoSuchAlgorithmException,
        SignatureException, InvalidKeyException, InvalidAlgorithmParameterException {

        String certBase64Url = Base64.getUrlEncoder().encodeToString(
            Base64.getDecoder().decode(SID_SIGNING_CERTIFICATE)
        );

        SDJWT sdJwt = SDJWT.parse(SESSION_TOKEN_BASE64URL);
        SignedJWT signedJWT = SignedJWT.parse(sdJwt.getCredentialJwt());
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        String rpChallengeBase64 = claimsSet.getClaimAsString("rpChallenge");
        String interactionsDigestBase64 = claimsSet.getClaimAsString("interactionsDigest");
        String interactionTypeUsed = claimsSet.getClaimAsString("interactionTypeUsed");

        GetSidSession.Signature signature = OBJECT_MAPPER.convertValue(
            claimsSet.getClaim("signature"),
            GetSidSession.Signature.class
        );
        String signatureValueBase64 = signature.value();
        String serverRandomBase64 = signature.serverRandom();
        String userChallengeBase64Url = signature.userChallenge();

        assertEquals(EXPECTED_INTERACTIONS_DIGEST_BASE64, interactionsDigestBase64);
        assertEquals(EXPECTED_RP_CHALLENGE_BASE64, rpChallengeBase64);
        assertEquals(EXPECTED_SID_SIGNATURE_BASE64, signatureValueBase64);
        assertEquals(EXPECTED_INTERACTION_TYPE_USED, interactionTypeUsed);
        assertEquals(EXPECTED_SERVER_RANDOM_BASE64, serverRandomBase64);
        assertEquals(EXPECTED_USER_CHALLENGE_BASE64URL, userChallengeBase64Url);

        X509Certificate cert = X509CertUtils.parse(Base64.getUrlDecoder().decode(
            certBase64Url
        ));


        String separator = "|";
        String schemeName = "smart-id";
        String signatureProtocol = "ACSP_V2";
        String relyingPartyNameBase64 = Base64.getEncoder()
            .encodeToString("DEMO".getBytes(StandardCharsets.UTF_8));
        String brokeredRpNameBase64 = "";
        String initialCallbackUrl = "";
        String flowType = signature.flowType();

        String[] payloadParts = {
            schemeName,
            signatureProtocol,
            serverRandomBase64,
            rpChallengeBase64,
            userChallengeBase64Url,
            relyingPartyNameBase64,
            brokeredRpNameBase64,
            interactionsDigestBase64,
            interactionTypeUsed,
            initialCallbackUrl,
            flowType
        };

        byte[] acspV2Payload = String
            .join(separator, payloadParts)
            .getBytes(StandardCharsets.UTF_8);

        MessageDigest md = MessageDigest.getInstance(
            signature.signatureAlgorithmParameters().hashAlgorithm()
        );
        md.update(acspV2Payload);
        byte[] acspDigest = md.digest();

        PublicKey publicKey = cert.getPublicKey();

        String maskGenDigestAlg =
            signature.signatureAlgorithmParameters().maskGenAlgorithm()
                .parameters().hashAlgorithm();

        PSSParameterSpec pssSpec = new PSSParameterSpec(
            signature.signatureAlgorithmParameters().hashAlgorithm(),
            "MGF1",                        // mask generation function
            new MGF1ParameterSpec(maskGenDigestAlg),
            signature.signatureAlgorithmParameters().saltLength(),
            PSSParameterSpec.TRAILER_FIELD_BC
        );

        Signature verifier = Signature.getInstance(signature.signatureAlgorithm());
        verifier.setParameter(pssSpec);
        verifier.initVerify(publicKey);
        verifier.update(acspDigest);

        byte[] sidSignatureBytes = Base64.getDecoder().decode(signatureValueBase64);
        boolean isVerified = verifier.verify(sidSignatureBytes);

        assertTrue(isVerified, "Signature verification failed");
    }
}
