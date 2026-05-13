package ee.cyber.cdoc2.server.app.usecase.startauth;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.auth.exception.InvalidEtsiSemanticsIdenfierException;
import ee.cyber.cdoc2.server.app.exception.InputValidationException;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartAuthImpl implements StartAuth {
    private static final int RP_CHALLENGE_BYTES_LENGTH = 64;

    private final StartSidAuth startSidAuth;
    private final StartMidAuth startMidAuth;

    @Override
    public Response execute(Request request) {
        EtsiIdentifier etsiIdentifier = getAndValidateEtsiIdentifier(request);

        UUID authUuid = UUID.randomUUID();
        byte[] rpChallenge = createRpChallengeBytes();
        String verificationCode;

        if (request.mobileNr() != null) {
            verificationCode = startMidAuth.execute(
                authUuid,
                rpChallenge,
                etsiIdentifier,
                request.mobileNr()
            );
        } else {
            verificationCode = startSidAuth.execute(
                authUuid,
                rpChallenge,
                etsiIdentifier
            );
        }

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

    private EtsiIdentifier getAndValidateEtsiIdentifier(StartAuth.Request request) {
        try {
            return new EtsiIdentifier(request.nationalId());
        } catch (InvalidEtsiSemanticsIdenfierException e) {
            log.warn("Error parsing ETSI identifier: {}", e.getMessage());
            throw new InputValidationException(e.getMessage(), e);
        }
    }
}
