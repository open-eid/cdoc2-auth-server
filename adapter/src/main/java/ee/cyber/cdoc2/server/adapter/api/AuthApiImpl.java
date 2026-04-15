package ee.cyber.cdoc2.server.adapter.api;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.generated.api.Cdoc2AuthApiDelegate;
import ee.cyber.cdoc2.server.adapter.generated.model.AuthIdentity;
import ee.cyber.cdoc2.server.adapter.generated.model.AuthProcessStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.StartAuthProcessResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.app.usecase.GetStatus;
import ee.cyber.cdoc2.server.app.usecase.startauth.StartAuth;

@Component
@RequiredArgsConstructor
public class AuthApiImpl implements Cdoc2AuthApiDelegate {
    private final StartAuth startAuth;
    private final GetStatus getStatus;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public ResponseEntity<StartAuthProcessResponse> startAuth(AuthIdentity authIdentity) {
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
    }

    @Override
    public ResponseEntity<AuthProcessStatusResponse> getAuthProcessStatus(String authProcessUuid) {
        String status = getStatus.execute(authProcessUuid);
        AuthProcessStatusResponse response = new AuthProcessStatusResponse(status);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WellKnownResponse> getWellKnown() {
        InputStream input = getClass()
            .getClassLoader()
            .getResourceAsStream("well-known-sample.json");

        WellKnownResponse response = OBJECT_MAPPER.readValue(input, WellKnownResponse.class);

        return ResponseEntity.ok(response);
    }

    private URI getAuthStatusProcessLocation(UUID authProcessUuid) {
        return URI.create("/auth/status/" + authProcessUuid);
    }
}
