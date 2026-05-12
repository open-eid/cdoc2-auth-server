package ee.cyber.cdoc2.server.app.usecase.startauth;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartAuthImpl implements StartAuth {
    private static final int RP_CHALLENGE_BYTES_LENGTH = 64;

    private final SessionNonce sessionNonce;
    private final StartSidAuth startSidAuth;
    private final StartMidAuth startMidAuth;

    @Override
    public Response execute(Request request) {
        UUID authUuid = UUID.randomUUID();
        byte[] rpChallenge = createRpChallengeBytes();
        String verificationCode;

        if (request.mobileNr() != null) {
            verificationCode = startMidAuth.doIt(authUuid, rpChallenge, request);
        } else {
            verificationCode = startSidAuth.doIt(authUuid, rpChallenge, request);
        }

        return new Response(
            authUuid,
            verificationCode
        );
    }

//    private String getAndValidatePhoneNumber(Request request) {
//        try {
//            Objects.requireNonNull(request.mobileNr());
//            return MidInputUtil.getValidatedPhoneNumber(request.mobileNr());
//        } catch (MidInvalidPhoneNumberException e) {
//            throw new InputValidationException(e.getMessage(), e);
//        }
//    }
//
//    private String getAndValidateNationalIdentityNumber(Request request) {
//        try {
//            return MidInputUtil.getValidatedNationalIdentityNumber(request.nationalId());
//        } catch (MidInvalidNationalIdentityNumberException e) {
//            throw new InputValidationException(e.getMessage(), e);
//        }
//    }

    private static byte[] createRpChallengeBytes() {
        byte[] rpChallengeBytes = new byte[RP_CHALLENGE_BYTES_LENGTH];
        new SecureRandom().nextBytes(rpChallengeBytes);
        return rpChallengeBytes;
    }

//    private Response performMidAuth(Request request) {
//        String validPhoneNumber = getAndValidatePhoneNumber(request);
//        String validNationalIdentityNumber = getAndValidateNationalIdentityNumber(request);
//
//        List<UriSessionNonce> sessionNonces = sessionNonce.collectSessionNonces();
//
//        SessionTokenCreationParams tokenCreationParams = new SessionTokenCreationParams(
//            sessionNonces,
//            validNationalIdentityNumber,
//            "https://cdoc2-auth-server.ee"
//        );
//
//        SDJWT unsignedSdJWT = SessionToken.unsignedSdJwtWithAllDisclosures(tokenCreationParams);
//
//        byte[] rpChallenge = createRpChallengeBytes();
//
//        MidHashToSign hashToSign = MidHashToSign.newBuilder()
//            .withDataToHash(rpChallenge)
//            .withHashType(MidHashType.SHA256)
//            .build();
//
//        byte[] hashBytes = hashToSign.getHash();
//
//        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.newBuilder()
//            .withHash(hashBytes)
//            .withHashType(MidHashType.SHA256)
//            .build();
//
//        UUID sessionId = midAuthenticate.execute(new MidAuthenticate.Request(
//                validPhoneNumber,
//                validNationalIdentityNumber,
//                authenticationHash
//            )
//        );
//        String verificationCode = authenticationHash.calculateVerificationCode();
//
//        return new Response(sessionId, verificationCode);
//    }
}
