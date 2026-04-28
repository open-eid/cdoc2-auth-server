package ee.cyber.cdoc2.server.app.usecase.startauth;


import ee.sk.smartid.VerificationCodeCalculator;
import ee.sk.smartid.common.InteractionsMapper;
import ee.sk.smartid.common.notification.interactions.NotificationInteraction;
import ee.sk.smartid.util.InteractionUtil;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.authlete.sd.SDJWT;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.server.app.usecase.common.SessionToken;
import ee.cyber.cdoc2.server.app.usecase.common.SessionToken.SessionTokenCreationParams;
import ee.cyber.cdoc2.server.app.usecase.startauth.SessionNonce.UriSessionNonce;

@Component
@RequiredArgsConstructor
public class StartAuthImpl implements StartAuth {
    private static final int RP_CHALLENGE_BYTES_LENGTH = 64;

    private final StoreAuthProcess storeAuthProcess;
    private final SessionNonce sessionNonce;
    private final SidAuthenticate sidAuthenticate;

    @Override
    public Response execute(Request request) {
        UUID authUuid = UUID.randomUUID();
        EtsiIdentifier etsiIdentifier = new EtsiIdentifier(request.nationalId());

        List<UriSessionNonce> sessionNonces = sessionNonce.collectSessionNonces();

        SessionTokenCreationParams tokenCreationParams = new SessionTokenCreationParams(
            sessionNonces,
            etsiIdentifier,
            "https://cdoc2-auth-server.ee"
        );

        SDJWT unsignedSdJWT = SessionToken.unsignedSdJwtWithAllDisclosures(tokenCreationParams);

        byte[] rpChallenge = createRpChallengeBytes();

        String verificationCode = VerificationCodeCalculator.calculate(rpChallenge);

        List<NotificationInteraction> interactions = List.of(
            NotificationInteraction
                .confirmationMessageAndVerificationCodeChoice("Creating CDOC2 session:"
                    + " " + etsiIdentifier.getSemanticsIdentifier())
        );

        String interactionsBase64 =
            InteractionUtil.encodeToBase64(InteractionsMapper.from(interactions));
        String interactionsDigest = InteractionUtil.calculateDigest(interactionsBase64);

        UUID sidAuthSessionUuid = sidAuthenticate.execute(new SidAuthenticate.Request(
            interactions,
            rpChallenge,
            etsiIdentifier.getSemanticsIdentifier()
        ));

        storeAuthProcess.execute(new StoreAuthProcess.Request(
            authUuid,
            sidAuthSessionUuid,
            unsignedSdJWT.toString(),
            interactionsDigest,
            Base64.getEncoder().encodeToString(rpChallenge)
        ));

        return new Response(
            authUuid,
            verificationCode
        );
    }

    private static byte[] createRpChallengeBytes() {
        byte[] rpChallengeBytes = new byte[RP_CHALLENGE_BYTES_LENGTH];
        new SecureRandom().nextBytes(rpChallengeBytes);
        return rpChallengeBytes;
    }
}
