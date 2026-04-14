package ee.cyber.cdoc2.server.app.usecase.status;

import java.util.Base64;

import com.authlete.sd.SDJWT;

final class SignSdJwt {

    private SignSdJwt() {
        // utility class
    }

    static String execute(String unsignedSdJwtString, String signatureValue) {
        SDJWT unsignedSdJwt = SDJWT.parse(unsignedSdJwtString);
        String credentialJwt = unsignedSdJwt.getCredentialJwt();

        String signedCredentialJwt =
            credentialJwt + "." + convertToBase64UrlEncoding(signatureValue);
        SDJWT signedSdJwt = new SDJWT(signedCredentialJwt, unsignedSdJwt.getDisclosures());

        return signedSdJwt.toString();
    }

    private static String convertToBase64UrlEncoding(String value) {
        byte[] sigDecoded = Base64.getDecoder().decode(value);
        return Base64.getUrlEncoder().encodeToString(sigDecoded);
    }
}
