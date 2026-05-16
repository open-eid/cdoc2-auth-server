package ee.cyber.cdoc2.server.app.usecase.status.sid;

import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.authlete.sd.SDJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;
import ee.cyber.cdoc2.server.app.usecase.status.JwtIssuanceClaimsCreator;
import ee.cyber.cdoc2.server.app.usecase.status.SdJwtSigner;

import static ee.cyber.cdoc2.server.app.Constants.RP_V3_SIGNATURE_ALGORITHM_NAME;

@Component
@RequiredArgsConstructor
class CreateSignedSdJwtForSid {
    private final RelyingPartyConf relyingPartyConf;
    private final SdJwtSigner sdJwtSigner;
    private final JwtIssuanceClaimsCreator jwtIssuanceClaimsCreator;

    String execute(String unsignedSdJwtString, SidSignatureParams params) {
        SDJWT unsignedSdJwt = SDJWT.parse(unsignedSdJwtString);
        String credentialJwt = unsignedSdJwt.getCredentialJwt();

        try {
            JWTClaimsSet claims = JWTClaimsSet.parse(credentialJwt);
            Map<String, Object> claimsMap = claims.toJSONObject();

            claimsMap.put("signatureProtocol", RP_V3_SIGNATURE_ALGORITHM_NAME);
            claimsMap.put("rpChallenge", params.rpChallenge());
            claimsMap.put("interactionsDigest", params.interactionsDigest());
            claimsMap.put("interactionTypeUsed", params.interactionTypeUsed());
            claimsMap.put("rpName", relyingPartyConf.getSidName());
            claimsMap.put("schemeName", relyingPartyConf.getSchemeName());
            claimsMap.put("signature", params.sidSignature());

            JWTClaimsSet claimsWithIssuance = jwtIssuanceClaimsCreator.execute(claimsMap);

            return sdJwtSigner.sign(claimsWithIssuance, unsignedSdJwt.getDisclosures());

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    record SidSignatureParams(
        GetSidSession.Signature sidSignature,
        String rpChallenge,
        String interactionsDigest,
        String interactionTypeUsed
    ) {
    }
}
