package ee.cyber.cdoc2.server.app.usecase.startauth;

import ee.sk.smartid.VerificationCodeCalculator;
import ee.sk.smartid.common.InteractionsMapper;
import ee.sk.smartid.common.notification.interactions.NotificationInteraction;
import ee.sk.smartid.util.InteractionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.authlete.sd.SDJWT;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;
import ee.cyber.cdoc2.server.app.usecase.common.SessionToken;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartSidAuth {
    private final StoreAuthProcess storeAuthProcess;
    private final SessionNonce sessionNonce;
    private final SidAuthenticate sidAuthenticate;

    String execute(UUID authProcessUuid, byte[] rpChallenge, EtsiIdentifier etsiIdentifier) {
        List<SessionNonce.UriSessionNonce> sessionNonces = sessionNonce.collectSessionNonces();

        SessionToken.SessionTokenCreationParams tokenCreationParams = new SessionToken.SessionTokenCreationParams(
            sessionNonces,
            etsiIdentifier.toString(),
            "https://cdoc2-auth-server.ee"
        );

        SDJWT unsignedSdJWT = SessionToken.unsignedSdJwtWithAllDisclosures(tokenCreationParams);

        String verificationCode = VerificationCodeCalculator.calculate(rpChallenge);

        List<NotificationInteraction> interactions = List.of(
            NotificationInteraction
                .confirmationMessageAndVerificationCodeChoice("Creating CDOC2 session:"
                    + " " + etsiIdentifier.getSemanticsIdentifier())
        );

        String interactionsBase64 =
            InteractionUtil.encodeToBase64(InteractionsMapper.from(interactions));
        String interactionsDigest = InteractionUtil.calculateDigest(interactionsBase64);

        UUID sessionId = sidAuthenticate.execute(new SidAuthenticate.Request(
            interactions,
            rpChallenge,
            etsiIdentifier.getSemanticsIdentifier()
        ));

        storeAuthProcess.execute(new StoreAuthProcess.Request(
            authProcessUuid,
            AuthProcessType.SID,
            sessionId,
            unsignedSdJWT.toString(),
            interactionsDigest,
            Base64.getEncoder().encodeToString(rpChallenge)
        ));

        return verificationCode;
    }
}
