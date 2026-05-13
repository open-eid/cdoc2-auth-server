package ee.cyber.cdoc2.server.app.usecase.status.mid;

import lombok.RequiredArgsConstructor;

import java.text.ParseException;

import org.springframework.stereotype.Component;

import com.authlete.sd.SDJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import ee.cyber.cdoc2.server.app.usecase.status.JwtIssuanceClaimsCreator;
import ee.cyber.cdoc2.server.app.usecase.status.SdJwtSigner;

@Component
@RequiredArgsConstructor
public class CreateSignedSdJwtForMid {
    private final JwtIssuanceClaimsCreator jwtIssuanceClaimsCreator;
    private final SdJwtSigner sdJwtSigner;

    String execute(String unsignedSdJwtString) {
        SDJWT unsignedSdJwt = SDJWT.parse(unsignedSdJwtString);
        String credentialJwt = unsignedSdJwt.getCredentialJwt();

        try {
            JWTClaimsSet claims = JWTClaimsSet.parse(credentialJwt);

            JWTClaimsSet claimsWithIssuance = jwtIssuanceClaimsCreator.execute(
                claims.toJSONObject()
            );

            return sdJwtSigner.sign(claimsWithIssuance, unsignedSdJwt.getDisclosures());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
