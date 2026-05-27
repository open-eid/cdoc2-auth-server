package ee.cyber.cdoc2.server.app.usecase.startauth;


import ee.sk.mid.MidAuthenticationHashToSign;

import java.util.UUID;

public interface MidAuthenticate {
    UUID execute(Request request);

    record Request(
        String phoneNumber,
        String nationalIdentityNumber,
        MidAuthenticationHashToSign authenticationHash,
        String displayText,
        Language language
    ) {
    }
}
