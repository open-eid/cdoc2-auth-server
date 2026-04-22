package ee.cyber.cdoc2.server.app.usecase.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.authlete.sd.SDObjectBuilder;
import com.nimbusds.jwt.JWTClaimsSet;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.server.app.usecase.startauth.SessionNonce.UriSessionNonce;

public final class SessionToken {
    private SessionToken() {
        // utility class
    }

    private static List<Disclosure> createDisclosureArray(List<UriSessionNonce> sessionNonceData) {
        return sessionNonceData.stream()
            .map(uriSessionNonce ->
                new Disclosure(uriSessionNonceToClaim(uriSessionNonce)) // generated salt, claimValue
            )
            .toList();
    }

    private static Disclosure genDisclosedAudience(List<Disclosure> audDisclosureArray) {
        return new Disclosure("aud",
            audDisclosureArray.stream()
                .map(Disclosure::toArrayElement
                ).toList()
        );
    }

    public static SDJWT unsignedSdJwtWithAllDisclosures(
        SessionTokenCreationParams creationParams
    ) {
        List<Disclosure> audArray = createDisclosureArray(creationParams.sessionNonceData);
        Disclosure audField = genDisclosedAudience(audArray);

        List<Disclosure> disclosures = new ArrayList<>();
        disclosures.add(audField);
        disclosures.addAll(audArray);

        JWTClaimsSet payload = createPayload(creationParams, audField);

        return new SDJWT(payload.toString(), disclosures);
    }

    private static JWTClaimsSet createPayload(
        SessionTokenCreationParams creationParams,
        Disclosure audDisclosureField
    ) {
        // Create an SDObjectBuilder instance to prepare the payload part of
        // a credential JWT. "sha-256" is used as a hash algorithm to compute
        // digest values of Disclosures.
        SDObjectBuilder builder = new SDObjectBuilder();

        // Put the digest value of the Disclosure.
        // will create claim  "_sd": ["y7ePxU9QuqYLz8ITcxcDAA4T43VDzF9_x2Z3z0-xxls"] that is calculated from
        // disclosed "aud" array, see getDisclosedShareAccessData()
        builder.putSDClaim(audDisclosureField);

        // Create a Map instance that represents the payload part of a
        // credential JWT. The 'claims' map contains the "_sd" array.
        // The size of the array is 1.
        Map<String, Object> claims = builder.build(true);

        JWTClaimsSet regularClaims = new JWTClaimsSet.Builder()
            .issuer(creationParams.issuer)
            .subject(creationParams.etsiIdentifier.toString())
            .build();

        claims.putAll(regularClaims.toJSONObject());

        JWTClaimsSet parsed;

        try {
            parsed = JWTClaimsSet.parse(claims);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse SDJWT claims");
        }

        // Prepare the payload part of a credential JWT.
        return parsed;
    }

    private static String uriSessionNonceToClaim(UriSessionNonce uriSessionNonce) {
        return uriSessionNonce.uri().toString() + "/" + uriSessionNonce.nonce();
    }

    public record SessionTokenCreationParams(
        List<UriSessionNonce> sessionNonceData,
        EtsiIdentifier etsiIdentifier,
        String issuer
    ) {
    }
}
