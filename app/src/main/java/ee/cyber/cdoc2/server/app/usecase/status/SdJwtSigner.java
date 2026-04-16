package ee.cyber.cdoc2.server.app.usecase.status;

import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.authlete.sd.SDJWT;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import ee.cyber.cdoc2.server.app.conf.JwtKeysConf;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

import static ee.cyber.cdoc2.server.app.Constants.RP_V3_SIGNATURE_ALGORITHM_NAME;
import static ee.cyber.cdoc2.server.app.Constants.SESSION_TOKEN_JWT_TYP;

@Component
@RequiredArgsConstructor
class SdJwtSigner {
    private final JwtKeysConf jwtKeysConf;

    String execute(String unsignedSdJwtString, SdJwtSignatureParams params) {
        SDJWT unsignedSdJwt = SDJWT.parse(unsignedSdJwtString);
        String credentialJwt = unsignedSdJwt.getCredentialJwt();

        try {
            JWTClaimsSet claims = JWTClaimsSet.parse(credentialJwt);
            Map<String, Object> claimsMap = claims.toJSONObject();

            claimsMap.put("signatureProtocol", RP_V3_SIGNATURE_ALGORITHM_NAME);
            claimsMap.put("rpChallenge", params.rpChallenge());
            claimsMap.put("interactionsDigest", params.interactionsDigest());
            claimsMap.put("interactionTypeUsed", params.interactionTypeUsed());
            claimsMap.put("signature", params.sidSignature());

            JWTClaimsSet claimsWithRpV3Data = JWTClaimsSet.parse(claimsMap);

            JWSHeader header =
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(jwtKeysConf.getEcKeyKid())
                    .type(new JOSEObjectType(SESSION_TOKEN_JWT_TYP))
                    .build();

            SignedJWT jwt = new SignedJWT(header, claimsWithRpV3Data);
            JWSSigner signer = new ECDSASigner(jwtKeysConf.ecPrivateKey());
            jwt.sign(signer);

            SDJWT signedSdJwt = new SDJWT(jwt.serialize(), unsignedSdJwt.getDisclosures());

            return signedSdJwt.toString();

        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    record SdJwtSignatureParams(
        GetSidSession.Signature sidSignature,
        String rpChallenge,
        String interactionsDigest,
        String interactionTypeUsed
    ) {
    }
}
