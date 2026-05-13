package ee.cyber.cdoc2.server.adapter.rest;

import ee.sk.mid.MidClient;
import ee.sk.mid.MidDisplayTextFormat;
import ee.sk.mid.MidLanguage;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.conf.MobileIdClientConf;
import ee.cyber.cdoc2.server.app.usecase.startauth.MidAuthenticate;

@Component
@RequiredArgsConstructor
public class MidRestClient implements MidAuthenticate {
    private final MidClient midClient;
    private final MobileIdClientConf.AppProperties properties;

    @Override
    public UUID execute(Request request) {
        MidAuthenticationRequest authenticationRequest = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(request.phoneNumber())
            .withNationalIdentityNumber(request.nationalIdentityNumber())
            .withHashToSign(request.authenticationHash())
            .withLanguage(MidLanguage.valueOf(properties.displayTextDefaultLang()))
            .withDisplayText(request.displayText())
            .withDisplayTextFormat(
                MidDisplayTextFormat.valueOf(properties.displayTextDefaultFormat())
            )
            .build();

        MidAuthenticationResponse response = midClient.getMobileIdConnector()
            .authenticate(authenticationRequest);

        return UUID.fromString(response.getSessionID());
    }
}
