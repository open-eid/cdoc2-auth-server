package ee.cyber.cdoc2.server.adapter.rest;

import ee.sk.mid.MidClient;
import ee.sk.mid.MidDisplayTextFormat;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.conf.MobileIdClientConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.Language;
import ee.cyber.cdoc2.server.app.usecase.startauth.MidAuthenticate;
import ee.cyber.cdoc2.server.app.usecase.status.mid.GetMidSession;

@Component
@RequiredArgsConstructor
public class MidRestClient implements MidAuthenticate, GetMidSession {
    private final MidClient midClient;
    private final MobileIdClientConf.AppProperties properties;

    private static MidLanguage toMidLanguage(Language language) {
        return switch (language) {
            case ET -> MidLanguage.EST;
            case RU -> MidLanguage.RUS;
            case EN -> MidLanguage.ENG;
        };
    }

    @Override
    public UUID execute(MidAuthenticate.Request request) {
        MidAuthenticationRequest authenticationRequest = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(request.phoneNumber())
            .withNationalIdentityNumber(request.nationalIdentityNumber())
            .withHashToSign(request.authenticationHash())
            .withLanguage(toMidLanguage(request.language()))
            .withDisplayText(request.displayText())
            .withDisplayTextFormat(
                MidDisplayTextFormat.valueOf(properties.displayTextFormat())
            )
            .build();

        MidAuthenticationResponse response = midClient.getMobileIdConnector()
            .authenticate(authenticationRequest);

        return UUID.fromString(response.getSessionID());
    }

    @Override
    public Response execute(UUID sessionId) {
        MidSessionStatus midSessionStatus = midClient.getMobileIdConnector()
            .getAuthenticationSessionStatus(
                new MidSessionStatusRequest(
                    sessionId.toString(),
                    properties.timeoutSeconds()
                )
            );

        return new Response(
            midSessionStatus.getState(),
            midSessionStatus.getResult(),
            midSessionStatus.getCert(),
            midSessionStatus.getSignature()
        );
    }
}
