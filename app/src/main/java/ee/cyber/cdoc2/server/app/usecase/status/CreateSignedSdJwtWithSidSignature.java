package ee.cyber.cdoc2.server.app.usecase.status;

import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

import static ee.cyber.cdoc2.server.app.Constants.RP_V3_SIGNATURE_ALGORITHM_NAME;
import static ee.cyber.cdoc2.server.app.Constants.SESSION_TOKEN_JWT_TYP;

@Component
@RequiredArgsConstructor
class CreateSignedSdJwtWithSidSignature {
    private final JwtKeysConf jwtKeysConf;
    private final RelyingPartyConf relyingPartyConf;
    private final Clock clock;

    String execute(String unsignedSdJwtString, SidSignatureParams params) {
        SDJWT unsignedSdJwt = SDJWT.parse(unsignedSdJwtString);
        String credentialJwt = unsignedSdJwt.getCredentialJwt();

        try {
            JWTClaimsSet claims = JWTClaimsSet.parse(credentialJwt);

            Instant now = clock.instant();

            JWTClaimsSet issuanceClaims = new JWTClaimsSet.Builder()
                .issueTime(Date.from(now))
                .expirationTime(
                    Date.from(now.plus(1, ChronoUnit.DAYS))
                )
                .build();
            Map<String, Object> issuanceClaimsMap = issuanceClaims.toJSONObject();

            Map<String, Object> claimsMap = claims.toJSONObject();
            claimsMap.putAll(issuanceClaimsMap);

            claimsMap.put("signatureProtocol", RP_V3_SIGNATURE_ALGORITHM_NAME);
            claimsMap.put("rpChallenge", params.rpChallenge());
            claimsMap.put("interactionsDigest", params.interactionsDigest());
            claimsMap.put("interactionTypeUsed", params.interactionTypeUsed());
            claimsMap.put("rpName", relyingPartyConf.getSidName());
            claimsMap.put("schemeName", relyingPartyConf.getSchemeName());
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

        } catch (ParseException | JOSEException e) {
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
