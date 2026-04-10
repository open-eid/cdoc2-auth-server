package ee.cyber.cdoc2.server.app.usecase.startauth;


import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import com.authlete.sd.SDJWT;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.SessionNonce.UriSessionNonce;
import ee.cyber.cdoc2.server.app.usecase.startauth.sid.NotificationVerificationCode;
import ee.cyber.cdoc2.server.app.usecase.common.SessionToken;
import ee.cyber.cdoc2.server.app.usecase.common.SessionToken.SessionTokenCreationParams;

@NullMarked
@RequiredArgsConstructor
@Component
public class StartAuthImpl implements StartAuth {
    private final StoreAuthProcess storeAuthProcess;
    private final SessionNonce sessionNonce;
    private final SessionNonceUriConf sessionNonceUriConf;
    private final SidAuthenticate sidAuthenticate;

    @Override
    public Response execute(Request request) {
        UUID authUuid = UUID.randomUUID();
        EtsiIdentifier etsiIdentifier = new EtsiIdentifier(request.nationalId());

        List<URI> sessionNonceUris = sessionNonceUriConf.getUris();
        List<UriSessionNonce> sessionNonces = sessionNonce.collectSessionNonces(sessionNonceUris);

        SessionTokenCreationParams tokenCreationParams = new SessionTokenCreationParams(
            sessionNonces,
            etsiIdentifier,
            "https://cdoc2-auth-server.ee"
        );

        SDJWT unsignedSdJWT = SessionToken.unsignedSdJwtWithAllDisclosures(tokenCreationParams);

        byte[] rpChallenge = SessionToken.getHashForCredentialJwt(unsignedSdJWT);

        String verificationCode = NotificationVerificationCode.create(
            rpChallenge
        );

        UUID sidAuthSessionUuid = sidAuthenticate.execute(new SidAuthenticate.Request(
            "",
            rpChallenge,
            etsiIdentifier.getSemanticsIdentifier()
        ));

        storeAuthProcess.execute(new StoreAuthProcess.Request(
            authUuid,
            sidAuthSessionUuid,
            sessionNonces,
            unsignedSdJWT.toString()
        ));

        return new Response(
            authUuid,
            verificationCode
        );
    }
}
