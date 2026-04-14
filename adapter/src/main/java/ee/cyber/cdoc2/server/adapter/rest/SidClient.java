package ee.cyber.cdoc2.server.adapter.rest;

import ee.sk.smartid.AuthenticationCertificateLevel;
import ee.sk.smartid.HashAlgorithm;
import ee.sk.smartid.RpChallenge;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.dao.SessionSignature;
import ee.sk.smartid.rest.dao.SessionSignatureAlgorithmParameters;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.signature.AuthenticationSignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.SidAuthenticate;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

@NullMarked
@Component
@RequiredArgsConstructor
public class SidClient implements SidAuthenticate, GetSidSession {
    private final SmartIdClient smartIdClient;
    private final RelyingPartyConf relyingPartyConf;

    public UUID execute(Request request) {
        RpChallenge rpChallenge = new RpChallenge(request.rpChallenge());

        var authenticationSessionResponse = smartIdClient.createNotificationAuthentication()
            .withRpChallenge(rpChallenge.toBase64EncodedValue())
            .withRelyingPartyUUID(String.valueOf(relyingPartyConf.getUuid()))
            .withRelyingPartyName(relyingPartyConf.getName())
            .withInteractions(request.interactions())
            .withHashAlgorithm(HashAlgorithm.SHA_256)
            .withSignatureAlgorithm(AuthenticationSignatureAlgorithm.RSASSA_PSS)
            .withCertificateLevel(AuthenticationCertificateLevel.QUALIFIED)
//            .withSemanticsIdentifier(new SemanticsIdentifier(request.semanticsIdentifier()))
            .withDocumentNumber(request.semanticsIdentifier())
            .initAuthenticationSession();

        return UUID.fromString(authenticationSessionResponse.sessionID());
    }

    @Override
    public GetSidSession.Response execute(UUID sessionId) {
        SessionStatusPoller poller = smartIdClient.getSessionStatusPoller();

        SessionStatus sessionStatus = poller.getSessionStatus(String.valueOf(sessionId));

        SessionSignature signature = sessionStatus.getSignature();
        SessionSignatureAlgorithmParameters signatureAlgorithmParameters = signature != null
            ? signature.getSignatureAlgorithmParameters() : null;

        return new GetSidSession.Response(
            sessionStatus.getState(),
            sessionStatus.getResult() != null ? sessionStatus.getResult().getEndResult()
                : null,
            signature != null
                ? new Signature(
                signature.getValue(),
                signature.getServerRandom(),
                signature.getUserChallenge(),
                signature.getSignatureAlgorithm(),
                signature.getFlowType(),
                new SignatureAlgorithmParameters(
                    signatureAlgorithmParameters.getHashAlgorithm(),
                    new MaskGenAlgorithm(
                        signatureAlgorithmParameters.getMaskGenAlgorithm().getAlgorithm(),
                        new MaskGenAlgorithm.Parameters(
                            signatureAlgorithmParameters.getMaskGenAlgorithm()
                            .getParameters().getHashAlgorithm()
                        )
                    ),
                    signatureAlgorithmParameters.getSaltLength(),
                    signatureAlgorithmParameters.getTrailerField()
                )
            )
                : null,
            sessionStatus.getCert() != null
                ? new Certificate(
                sessionStatus.getCert().getValue(),
                sessionStatus.getCert().getCertificateLevel()
            )
                : null,
            sessionStatus.getInteractionTypeUsed()
        );
    }
}
