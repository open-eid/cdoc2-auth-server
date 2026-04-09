package ee.cyber.cdoc2.server.adapter.rest;

import ee.sk.smartid.AuthenticationCertificateLevel;
import ee.sk.smartid.HashAlgorithm;
import ee.sk.smartid.RpChallenge;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.common.notification.interactions.NotificationInteraction;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.signature.AuthenticationSignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.usecase.SidSession;
import ee.cyber.cdoc2.server.app.usecase.startauth.SidAuthenticate;

@NullMarked
@Component
@RequiredArgsConstructor
public class SidClient implements SidAuthenticate, SidSession {
    public static final UUID DEMO_RP_UUID = UUID.fromString("00000000-0000-4000-8000-000000000000");
    public static final String DEMO_RP_NAME = "DEMO";

    private final SmartIdClient smartIdClient;

    public UUID execute(Request request) {
        RpChallenge rpChallenge = new RpChallenge(request.rpChallenge());

        var authenticationSessionResponse = smartIdClient.createNotificationAuthentication()
            .withRpChallenge(rpChallenge.toBase64EncodedValue())
            .withRelyingPartyUUID(String.valueOf(DEMO_RP_UUID))
            .withRelyingPartyName(DEMO_RP_NAME)
            .withInteractions(List.of(
                NotificationInteraction
                    .confirmationMessageAndVerificationCodeChoice("Creating CDOC2 session")
            ))
            .withHashAlgorithm(HashAlgorithm.SHA_256)
            .withSignatureAlgorithm(AuthenticationSignatureAlgorithm.RSASSA_PSS)
            .withCertificateLevel(AuthenticationCertificateLevel.QUALIFIED)
//            .withSemanticsIdentifier(new SemanticsIdentifier(request.semanticsIdentifier()))
            .withDocumentNumber(request.semanticsIdentifier())
            .initAuthenticationSession();

        return UUID.fromString(authenticationSessionResponse.sessionID());
    }

    public SidSession.Response execute(UUID sessionId) {
        SessionStatusPoller poller = smartIdClient.getSessionStatusPoller();

        SessionStatus sessionStatus = poller.getSessionStatus(String.valueOf(sessionId));

        return new Response(
            sessionStatus.getState(),
            sessionStatus.getResult().getEndResult(),
            sessionStatus.getSignature() != null
                ? new SessionSignature(
                sessionStatus.getSignature().getValue(),
                sessionStatus.getSignature().getServerRandom(),
                sessionStatus.getSignature().getUserChallenge()
            )
                : null
        );
    }
}
