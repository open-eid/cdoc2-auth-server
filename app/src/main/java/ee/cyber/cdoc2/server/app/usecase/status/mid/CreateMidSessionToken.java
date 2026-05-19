package ee.cyber.cdoc2.server.app.usecase.status.mid;

import ee.sk.mid.MidAuthentication;
import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidAuthenticationResponseValidator;
import ee.sk.mid.MidAuthenticationResult;
import ee.sk.mid.MidCertificateParser;
import ee.sk.mid.rest.dao.MidSessionSignature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.usecase.common.MidUtil;
import ee.cyber.cdoc2.server.app.usecase.status.GetSessionTokenMaterial;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateMidSessionToken {
    private final CreateSignedSdJwtForMid createSignedSdJwtForMid;
    private final GetSessionTokenMaterial getSessionTokenMaterial;
    private final MidAuthenticationResponseValidator responseValidator;

    public String execute(UUID authProcessUuid, GetMidSession.@Nullable Response sessionResponse) {
        GetMidSession.Response midSessionResponse = validateSessionResponse(sessionResponse);
        validateSessionSignatureNotBlank(midSessionResponse.signature());

        GetSessionTokenMaterial.Response sessionTokenMaterial =
            getSessionTokenMaterial.execute(
                new GetSessionTokenMaterial.Request(authProcessUuid)
            );

        validateSignature(
            sessionTokenMaterial.rpChallenge(),
            midSessionResponse
        );

        return createSignedSdJwtForMid.execute(
            sessionTokenMaterial.unsignedJwt()
        );
    }

    private GetMidSession.Response validateSessionResponse(
        GetMidSession.@Nullable Response sessionResponse
    ) {
        if (sessionResponse == null) {
            throw new IllegalStateException("MID session response missing");
        }

        return sessionResponse;
    }

    private void validateSignature(String rpChallenge, GetMidSession.Response sessionResponse) {
        X509Certificate certificate = MidCertificateParser.parseX509Certificate(
            sessionResponse.cert()
        );

        MidSessionSignature sessionSignature = sessionResponse.signature();

        MidAuthenticationHashToSign hashSigned = MidUtil.createAuthenticationHash(
            Base64.getDecoder().decode(rpChallenge)
        );

        MidAuthentication midAuthentication = MidAuthentication.newBuilder()
            .withResult(sessionResponse.endResult())
            .withSignatureValueInBase64(sessionSignature.getValue())
            .withAlgorithmName(sessionSignature.getAlgorithm())
            .withCertificate(certificate)
            .withSignedHashInBase64(hashSigned.getHashInBase64())
            .withHashType(hashSigned.getHashType())
            .build();

        MidAuthenticationResult authResult = responseValidator.validate(midAuthentication);
        List<String> authErrors = authResult.getErrors();
        if (!authResult.isValid() || !authErrors.isEmpty()) {
            for (String error : authErrors) {
                log.error(error);
            }
            throw new IllegalStateException("MID signature validation failed");
        }
    }

    private void validateSessionSignatureNotBlank(@Nullable MidSessionSignature signature) {
        if (signature == null || isBlank(signature.getValue())) {
            log.error("Signature was not present in the response");
            throw new IllegalStateException("Signature was not present in the response");
        }
    }
}
