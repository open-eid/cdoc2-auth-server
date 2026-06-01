package ee.cyber.cdoc2.server.adapter.rest;

import ee.sk.smartid.AuthenticationCertificateLevel;
import ee.sk.smartid.HashAlgorithm;
import ee.sk.smartid.RpChallenge;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.exception.UserAccountException;
import ee.sk.smartid.exception.UserActionException;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.dao.SemanticsIdentifier;
import ee.sk.smartid.rest.dao.SessionSignature;
import ee.sk.smartid.rest.dao.SessionSignatureAlgorithmParameters;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.signature.AuthenticationSignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;
import ee.cyber.cdoc2.server.app.CertificateLevel;
import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.SidAuthenticate;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

@Component
@RequiredArgsConstructor
public class SidRestClient implements SidAuthenticate, GetSidSession {
    private static final String SID_CLIENT_ERROR_CODE = "SID_CLIENT_ERROR";

    private final SmartIdClient smartIdClient;
    private final RelyingPartyConf relyingPartyConf;

    public UUID execute(Request request) {
        RpChallenge rpChallenge = new RpChallenge(request.rpChallenge());

        try {
            var authenticationSessionResponse = smartIdClient.createNotificationAuthentication()
                .withRpChallenge(rpChallenge.toBase64EncodedValue())
                .withRelyingPartyUUID(String.valueOf(relyingPartyConf.getSidUuid()))
                .withRelyingPartyName(relyingPartyConf.getSidName())
                .withInteractions(request.interactions())
                .withHashAlgorithm(HashAlgorithm.SHA_256)
                .withSignatureAlgorithm(AuthenticationSignatureAlgorithm.RSASSA_PSS)
                .withCertificateLevel(convertCertificateLevel(relyingPartyConf.getCertificateLevel()))
                .withSemanticsIdentifier(new SemanticsIdentifier(request.semanticsIdentifier()))
                .initAuthenticationSession();

            return UUID.fromString(authenticationSessionResponse.sessionID());
        } catch (UserAccountException | UserActionException e) {
            throw new ClientBadRequestException(SID_CLIENT_ERROR_CODE, e.getMessage());
        }
    }

    @Override
    public GetSidSession.Response execute(UUID sessionId) {
        try {
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
        } catch (UserAccountException | UserActionException e) {
            throw new ClientBadRequestException(SID_CLIENT_ERROR_CODE, e.getMessage());
        }
    }

    private AuthenticationCertificateLevel convertCertificateLevel(
        CertificateLevel certificateLevel
    ) {
        if (certificateLevel == CertificateLevel.QUALIFIED) {
            return AuthenticationCertificateLevel.QUALIFIED;
        }
        if (certificateLevel == CertificateLevel.ADVANCED) {
            return AuthenticationCertificateLevel.ADVANCED;
        }

        throw new IllegalArgumentException("Unknown certificate level");
    }
}
