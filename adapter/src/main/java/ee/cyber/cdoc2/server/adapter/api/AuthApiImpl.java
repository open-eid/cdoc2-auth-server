package ee.cyber.cdoc2.server.adapter.api;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Base64;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.conf.WellKnownJwkConf;
import ee.cyber.cdoc2.server.adapter.exception.AuthProcessNotFoundException;
import ee.cyber.cdoc2.server.adapter.generated.api.Cdoc2AuthApiDelegate;
import ee.cyber.cdoc2.server.adapter.generated.model.AuthIdentity;
import ee.cyber.cdoc2.server.adapter.generated.model.AuthProcessStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.StartAuthProcessResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.app.exception.InputValidationException;
import ee.cyber.cdoc2.server.app.usecase.startauth.StartAuth;
import ee.cyber.cdoc2.server.app.usecase.status.GetStatus;

@Component
@RequiredArgsConstructor
public class AuthApiImpl implements Cdoc2AuthApiDelegate {
    private final StartAuth startAuth;
    private final GetStatus getStatus;
    private final WellKnownJwkConf wellKnownJwkConf;

    @Override
    public ResponseEntity<StartAuthProcessResponse> startAuth(AuthIdentity authIdentity) {
        try {
            StartAuth.Response response =
                startAuth.execute(new StartAuth.Request(
                    authIdentity.getIdentifier(),
                    authIdentity.getMobileNr()
                ));

            StartAuthProcessResponse responseBody = new StartAuthProcessResponse(
                response.verificationCode()
            );

            return ResponseEntity.created(getAuthStatusProcessLocation(
                    response.uuid()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody);
        } catch (InputValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<AuthProcessStatusResponse> getAuthProcessStatus(String authProcessUuid) {
        try {
            GetStatus.Response response = getStatus.execute(authProcessUuid);

            AuthProcessStatusResponse responseBody = new AuthProcessStatusResponse(response.status())
                .endResult(response.endResult())
                .sessionToken(response.sessionToken())
                .signingCertificate(
                    base64toBase64Url(response.signingCertificate())
                );

            return ResponseEntity.ok(responseBody);
        } catch (AuthProcessNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<WellKnownResponse> getWellKnown() {
        return ResponseEntity.ok(wellKnownJwkConf.getJwkResponse());
    }

    //TODO should be created dynamically based on controller URI
    private URI getAuthStatusProcessLocation(UUID authProcessUuid) {
        return URI.create("/auth/status/" + authProcessUuid);
    }

    private static String base64toBase64Url(String base64String) {
        if (base64String == null) {
            return null;
        }

        return Base64.getUrlEncoder().encodeToString(
            Base64.getDecoder().decode(base64String)
        );
    }
}
