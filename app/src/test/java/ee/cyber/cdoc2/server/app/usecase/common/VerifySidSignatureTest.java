package ee.cyber.cdoc2.server.app.usecase.common;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerifySidSignatureTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // SESSION_TOKEN_BASE64URL, SID_SIGNING_CERTIFICATE are from auth/status endpoint response
    @SuppressWarnings("checkstyle:LineLength")
    private static final String SESSION_TOKEN_BASE64URL =
        "eyJraWQiOiJlYy1rZXktMjAyNiIsInR5cCI6InZuZC5jZG9jMi5zZXNzaW9uLXRva2VuLnYyK3NkLWp3dCIsImFsZyI6IkVTMjU2In0.eyJycENoYWxsZW5nZSI6ImlsU1lHN0VzQXVQSURSNG1uZ2hIVVpEM2NRREVMZGNadjNyc2dVSG1jYmRQenRPVVN2bTIxY3hRTlJUTm5BUkVNOTBPcks2UDN2c0RKa25GVndtQVp3PT0iLCJfc2QiOlsiWWl2V01GWTExdkNBQ3V1enJoRGZJcndCOS1WTmMyZlhxdlpRUmdWbVJ3WSJdLCJzdWIiOiJldHNpL1BOT0VFLTQwNTA0MDQwMDAxLURFTTAtUSIsImludGVyYWN0aW9uc0RpZ2VzdCI6ImNjek4wL2ZUS2FvRmdmQVllTnhTTFJtWFFvVWcybEYxb0N6VVRYTkpJblk9Iiwic2lnbmF0dXJlIjp7InZhbHVlIjoiRzQzZFBvTW9mcVJvVHZFOG8wMUdLTzRaZ2JPYy9mUTZnd051U1hndEIraXNFelorMFhwdHJuSExhL3YyWVRGZTVnNEZSRGpVaDR0SXdxSGU0enNFdnBTMWZIU1F2a0JBZEpUbEthbXY5Y1VNVzBDemFjKzh3UUJzUjNlSVZVZTg5MFdpQWVjL3VXVVM1TUxQTkFvZVJPZ2VrVVd3WnkrekNLOFJzUFByekZwRWV5d2hCSzZmRnNKcTlNZkdzL2tuSE4wS1RjQ0ljbG9hSnhPbG4yZ0JTa28zOXgraEh3VjQ3SDVIMXlqYjN0dlpSMjdxR1NTUFRPRGFYT3BXQVNvZjFhQ3NyTEltV2VYUXRyNWVTYktUcUlQMStYZGhFa2hWZG9CMUxmZ2pBWUJKWVRBQXVsaUJxRVZqdStRcFQ0T0VLWWlaV2dvemdqVjM3dVBQQWd2ak0zYmtGa3BzZTJYcm5tYnExRlU3MFZ1N2NKNFpFTzh0bVl4QVdRRjRGREpBRGE1ajVyWG5vN3R0ck5GZDRVdHdubThNTmJ4T2NBT08yZ1NRQzBPVEdQaVQwbnU3d1Z6SXFxTDlLcytNYjl6b251VEpzeUlqeTdCT3BFZ1RqOW91MlQ2d3ZOY1cxa2pOL2xCeU9zaUxpMGxESk0zS1NCV05IOGZ3dEIrV0hGdjZRalBRVlZCdXJLVjhsLzlOWExLQnFHKzNTc1ovVHpMUkpPM24rdk0rRk5ORFBtcE9RTzAyN1hTd3dUamU2ZW4zRTFIbzFNb05DVzJBdHJaamNOMkI0dEVrS3FwQkxwRnRoN1ZnanZMTGhJOENYWHZxZzBnVmdjTjJMZHJtZkU4N1JIM2dScGdCOHZ4ZktBZjJMTm53b1VXaVZ0TXBDS2wyZDZlenMwZVNLY1pKMFY0UmMwZ2luU3NMT25TQTByeDNDNWl5Um5WMXhVOWRSSEtiVGhhb0ltNFpwV0txTjB4ZHdXeVM0ZmgwdTd0alNZNnJaRGE3dEtNWkc1SjRrQklXUnB3QWxiQWt3cUp4RDIzVTFSaUltV2E1UGx2NU8zbWhXcmdvZnJxcGJOemRVV0lCbVQrSWhZWGRRUExUYm9GaU1RYm9qakhERkxTR1o5aldIU0l2cGkrQ2piblNySGs3V3FibFEzMGVELzNmY2V2R1J1dGFmdzdvb3hGMGpjOVhyVC9VWGRLTEd0TllyazFIcVhsKzZjZVRKcWNyYURjaFh3NTJlSXFHT1hCbEtuVjF5VmtRV0JRV1RielpFc2ZEbE4yZ0hlUU9xTlBEM2QveVVYNkJmTHZUeHVBWmlpSEpDWTZrbFdPNlhhSEtQZEZNQk1UWWJWYzdyYXZDaGwvQyIsInNlcnZlclJhbmRvbSI6InpiVk90b3VuZU1EMzhTZmJvcG5PeGh6WSIsInVzZXJDaGFsbGVuZ2UiOiIzUjE3RHJoTXRRc3lGY3JtZklSa1dvczJUMVFyTlRlcHZCRWdsNFNxbTJrIiwic2lnbmF0dXJlQWxnb3JpdGhtIjoicnNhc3NhLXBzcyIsImZsb3dUeXBlIjoiTm90aWZpY2F0aW9uIiwic2lnbmF0dXJlQWxnb3JpdGhtUGFyYW1ldGVycyI6eyJoYXNoQWxnb3JpdGhtIjoiU0hBLTI1NiIsIm1hc2tHZW5BbGdvcml0aG0iOnsiYWxnb3JpdGhtIjoiaWQtbWdmMSIsInBhcmFtZXRlcnMiOnsiaGFzaEFsZ29yaXRobSI6IlNIQS0yNTYifX0sInNhbHRMZW5ndGgiOjMyLCJ0cmFpbGVyRmllbGQiOiIweGJjIn19LCJfc2RfYWxnIjoic2hhLTI1NiIsImlzcyI6Imh0dHBzOi8vY2RvYzItYXV0aC1zZXJ2ZXIuZWUiLCJzaWduYXR1cmVQcm90b2NvbCI6IlJTQVNTQS1QU1MrQUNTUF9WMiIsImV4cCI6MTc3NjQyNTM2NCwiaWF0IjoxNzc2MzM4OTY0LCJpbnRlcmFjdGlvblR5cGVVc2VkIjoiY29uZmlybWF0aW9uTWVzc2FnZUFuZFZlcmlmaWNhdGlvbkNvZGVDaG9pY2UifQ.B3YkJEvF_88XxiNaQsDVHBiMuvAKiXpVmG1XHXvGVyM1NlFFCav_LqSsKOD2vyCjcq628itT4lbuC1N5qHsU3g~WyIzR2ZSU3BMVzRDOFh1aEw4WEhRcHN3IiwiYXVkIixbeyIuLi4iOiJhRnFVOThGa0RRdDluXy12YzhDUEhyWGhUakpFaXFLanluOWd5QVRMMHRNIn0seyIuLi4iOiJfcW5KYjVkYTR3ZV91cmV2Szl2cmtSVC13a3NlR1FsYkg5aUVVVzlfWkVrIn1dXQ~WyI1ZnlQcjF0Yk5DOEdtMlRLMjZQSHZnIiwiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3Nlc3Npb25fbm9uY2VfMS8xMjM0NTY3ODkwOTg3NjU0MzIxIl0~WyIwS24yYXFlM0dxTE1YbHhfbFNzLTF3IiwiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3Nlc3Npb25fbm9uY2VfMi85ODc2NTQzMjEyMzQ1Njc4OSJd~";
    @SuppressWarnings("checkstyle:LineLength")
    private static final String SID_SIGNING_CERTIFICATE_BASE64URL =
        "MIIGpzCCBi6gAwIBAgIQGcJUbe6JHI6jJyV-42vjnTAKBggqhkjOPQQDAzBxMSwwKgYDVQQDDCNURVNUIG9mIFNLIElEIFNvbHV0aW9ucyBFSUQtUSAyMDI0RTEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxGzAZBgNVBAoMElNLIElEIFNvbHV0aW9ucyBBUzELMAkGA1UEBhMCRUUwHhcNMjYwMTA2MTQyNTAxWhcNMjkwMTA1MTQyNTAwWjBXMQswCQYDVQQGEwJFRTEQMA4GA1UEAwwHVEVTVCxPSzENMAsGA1UEBAwEVEVTVDELMAkGA1UEKgwCT0sxGjAYBgNVBAUTEVBOT0VFLTQwNTA0MDQwMDAxMIIDIjANBgkqhkiG9w0BAQEFAAOCAw8AMIIDCgKCAwEAkI98VzyaeSueyaUQYIXMMf-1VY10Gw-b8Q13Rb9N62ROZY97wMIB__f8_PuOIoqkAPM6Tn_t4lp1R_rHrbuqs0hl2dgLlOcR5wmWmp7YfKPDvRndVLl_doIHruxY8O60rFGskSnqt4coHN4xGcmCyPkJoB8Rfm8-Y9poVKAreS0Ta32p5OSME0HjSs7-ahB2erWfb2GulFw1vyeH42d3XDpCCfd6CByvSsi4oByUqs5G-kjSrGUglflgWXK3MxBYto0swgsbD1nrW5doU_cMCfRoFURun4XguX8dTt9VeyqeJitxRfub2Hj18RbsKuoFNHQNOxAxRK4oTVCtUrYbVqBHDmoOm8r3CsSuqjuZ2njQybiUhBofpTVMCZ6lB6VgoLphmEwSEOQXIumpmpb2qJZqbZaBoyyWb4f5AQjw3Q5lwPSao5215hIgSuuENRezpP9rTzIwyOMbnV2nMSMInAuaXIXskB2NdpMsROsvOqBC0h5azTj9naCS-5EW-9eI7GGK03Du5JoKD5wYajJxfcxFwBAl8Ko71OvhGFtYiu-hqzz-CyG6NswB87KvzDYUCQ-0qOfgRBNCgYnbjnuYVJb3CGLp_cP5GmKtUC3wHX1WnPGyK4bD19Rcy-FhG6mD_ZrAPcmZ3s4FLLErpRJ3ui-fiMPLQl2bpCKTWoaEZoPg6Grnhr3bE2ZiKWmqdVwf30bG3-GnvTBTuF0T1lzt6NeBlB23SJsffCmzSFSNcFJHHYI1FYdZu2p0gL6KAabEmnE8GrTrCn93DFNBtoKu9vG30QrRzyh-itPvtn9w-9t-nDkhaVHmNCjWD1xcMeXsyK8ek0rbz5aVe_RPvCifhIpgjqNsDHh9q1QT9KIFsd6RD2XPMlekL9c6YiVY9H7uRyIQWqJwtrvNvBKj4ZT9745zTfkhCJTPvnLy-4iKeINVZ2f98BblsGAEHKGol8YA-3SRkPh9BVnVhSdI3lxCDEbmHuk21GIPE9689efSvbcDEHpqeYoxo3tXjl_hqfzPAgMBAAGjggH1MIIB8TAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFLAkFxmI42b4zShYZXtNFNiSZk9rMHAGCCsGAQUFBwEBBGQwYjAzBggrBgEFBQcwAoYnaHR0cDovL2Muc2suZWUvVEVTVF9FSUQtUV8yMDI0RS5kZXIuY3J0MCsGCCsGAQUFBzABhh9odHRwOi8vYWlhLmRlbW8uc2suZWUvZWlkcTIwMjRlMDAGA1UdEQQpMCekJTAjMSEwHwYDVQQDDBhQTk9FRS00MDUwNDA0MDAwMS1ERU0wLVEweAYDVR0gBHEwbzBjBgkrBgEEAc4fEQIwVjBUBggrBgEFBQcCARZIaHR0cHM6Ly93d3cuc2tpZHNvbHV0aW9ucy5ldS9yZXNvdXJjZXMvY2VydGlmaWNhdGlvbi1wcmFjdGljZS1zdGF0ZW1lbnQvMAgGBgQAj3oBAjAoBgNVHQkEITAfMB0GCCsGAQUFBwkBMREYDzE5MDUwNDA0MTIwMDAwWjAWBgNVHSUEDzANBgsrBgEEAYPmYgUHADA0BgNVHR8ELTArMCmgJ6AlhiNodHRwOi8vYy5zay5lZS90ZXN0X2VpZC1xXzIwMjRlLmNybDAdBgNVHQ4EFgQUX9YaVGlPdUOO2J6rzNc4sljBQBAwDgYDVR0PAQH_BAQDAgeAMAoGCCqGSM49BAMDA2cAMGQCMHhYJCeKceJv_m0xcFRssS4WVFnnCryDiuSEpjDZu0irJ_XurXXIFDr-9hhl2x7GMwIwbiD5GALRtwzUaEh-SV9jigT9Oc336f6QYf8YaSA0-Un8eRQPa9wTK0cSQrM_CUIu";

    @Test
    void verifyRpV3Signature() throws ParseException, GeneralSecurityException {
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

        X509Certificate cert = X509CertUtils.parse(Base64.getUrlDecoder().decode(
            SID_SIGNING_CERTIFICATE_BASE64URL
        ));

        String separator = "|";
        String schemeName = "smart-id-demo";
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

        String acspV2Payload = String.join(separator, payloadParts);
        byte[] acspV2PayloadBytes = acspV2Payload.getBytes(StandardCharsets.UTF_8);

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
        verifier.update(acspV2PayloadBytes);

        byte[] sidSignatureBytes = Base64.getDecoder().decode(signatureValueBase64);
        boolean isVerified = verifier.verify(sidSignatureBytes);

        assertTrue(isVerified, "Signature verification failed");
    }
}
