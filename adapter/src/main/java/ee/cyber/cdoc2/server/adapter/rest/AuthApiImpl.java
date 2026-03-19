package ee.cyber.cdoc2.server.adapter.rest;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.generated.api.Cdoc2AuthApiDelegate;
import ee.cyber.cdoc2.server.adapter.generated.model.AuhtProcessStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.AuthIdentity;
import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.app.usecase.GetStatus;
import ee.cyber.cdoc2.server.app.usecase.StartAuth;

@Component
@RequiredArgsConstructor
public class AuthApiImpl implements Cdoc2AuthApiDelegate {
    private final StartAuth startAuth;
    private final GetStatus getStatus;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public ResponseEntity<Void> startAuth(AuthIdentity authIdentity) {
        UUID authProcessUd = startAuth.execute(new StartAuth.Request(
            authIdentity.getIdentifier(),
            authIdentity.getMobileNr()
        ));

        return ResponseEntity.created(getAuthStatusProcessLocation(authProcessUd)).build();
    }

    @Override
    public ResponseEntity<AuhtProcessStatusResponse> getAuthProcessStatus(String authProcessUuid) {
        String status = getStatus.execute(authProcessUuid);
        AuhtProcessStatusResponse response = new AuhtProcessStatusResponse(status);
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
