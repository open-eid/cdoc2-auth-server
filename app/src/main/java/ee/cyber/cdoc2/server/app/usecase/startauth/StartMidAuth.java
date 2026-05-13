package ee.cyber.cdoc2.server.app.usecase.startauth;

import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidInputUtil;
import ee.sk.mid.exception.MidInvalidNationalIdentityNumberException;
import ee.sk.mid.exception.MidInvalidPhoneNumberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.authlete.sd.SDJWT;

import ee.cyber.cdoc2.server.app.exception.InputValidationException;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;
import ee.cyber.cdoc2.server.app.usecase.common.MidUtil;
import ee.cyber.cdoc2.server.app.usecase.common.SessionToken;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartMidAuth {
    private final StoreAuthProcess storeAuthProcess;
    private final SessionNonce sessionNonce;
    private final MidAuthenticate midAuthenticate;

    String execute(UUID authProcessUuid, byte[] rpChallenge, StartAuth.Request request) {
        String validPhoneNumber = getAndValidatePhoneNumber(request);
        String validNationalIdentityNumber = getAndValidateNationalIdentityNumber(request);

        List<SessionNonce.UriSessionNonce> sessionNonces = sessionNonce.collectSessionNonces();

        SessionToken.SessionTokenCreationParams tokenCreationParams = new SessionToken.SessionTokenCreationParams(
            sessionNonces,
            validNationalIdentityNumber,
            "https://cdoc2-auth-server.ee"
        );

        SDJWT unsignedSdJWT = SessionToken.unsignedSdJwtWithAllDisclosures(tokenCreationParams);

        MidAuthenticationHashToSign authenticationHash = MidUtil.createAuthenticationHash(
            rpChallenge
        );

        UUID sessionId = midAuthenticate.execute(new MidAuthenticate.Request(
                validPhoneNumber,
                validNationalIdentityNumber,
                authenticationHash
            )
        );

        String verificationCode = authenticationHash.calculateVerificationCode();

        storeAuthProcess.execute(new StoreAuthProcess.Request(
            authProcessUuid,
            AuthProcessType.MID,
            sessionId,
            unsignedSdJWT.toString(),
            null,
            Base64.getEncoder().encodeToString(rpChallenge)
        ));

        return verificationCode;
    }

    private String getAndValidatePhoneNumber(StartAuth.Request request) {
        try {
            Objects.requireNonNull(request.mobileNr());
            return MidInputUtil.getValidatedPhoneNumber(request.mobileNr());
        } catch (MidInvalidPhoneNumberException e) {
            throw new InputValidationException(e.getMessage(), e);
        }
    }

    private String getAndValidateNationalIdentityNumber(StartAuth.Request request) {
        try {
            return MidInputUtil.getValidatedNationalIdentityNumber(request.nationalId());
        } catch (MidInvalidNationalIdentityNumberException e) {
            throw new InputValidationException(e.getMessage(), e);
        }
    }
}
