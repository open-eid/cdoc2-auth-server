package ee.cyber.cdoc2.server.app.usecase.status;

import com.authlete.sd.SDJWT;

final class SignSdJwt {

    private SignSdJwt() {
        // utility class
    }

    static String execute(String unsignedSdJwtString, String signatureValue) {
        SDJWT unsignedSdJwt = SDJWT.parse(unsignedSdJwtString);
        String credentialJwt = unsignedSdJwt.getCredentialJwt();

        String signedCredentialJwt = credentialJwt + "." + signatureValue;
        SDJWT signedSdJwt = new SDJWT(signedCredentialJwt, unsignedSdJwt.getDisclosures());

        return signedSdJwt.toString();
    }
}
