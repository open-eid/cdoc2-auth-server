package ee.cyber.cdoc2.server.app.usecase.status;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.authlete.sd.Disclosure;
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

import static ee.cyber.cdoc2.server.app.Constants.SESSION_TOKEN_JWT_TYP;

@Component
@RequiredArgsConstructor
public class SdJwtSigner {
    private final JwtKeysConf jwtKeysConf;

    public String sign(JWTClaimsSet claimsSet, List<Disclosure> disclosures) {
        JWSHeader header =
            new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(jwtKeysConf.getEcKeyKid())
                .type(new JOSEObjectType(SESSION_TOKEN_JWT_TYP))
                .build();

        SignedJWT jwt = new SignedJWT(header, claimsSet);

        try {
            JWSSigner signer = new ECDSASigner(jwtKeysConf.ecPrivateKey());
            jwt.sign(signer);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        SDJWT signedSdJwt = new SDJWT(jwt.serialize(), disclosures);

        return signedSdJwt.toString();
    }
}
