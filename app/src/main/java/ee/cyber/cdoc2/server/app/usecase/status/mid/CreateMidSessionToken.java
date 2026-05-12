package ee.cyber.cdoc2.server.app.usecase.status.mid;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.usecase.status.GetSessionTokenMaterial;

@Component
@RequiredArgsConstructor
public class CreateMidSessionToken {
    private final CreateSignedSdJwtForMid createSignedSdJwtForMid;
    private final GetSessionTokenMaterial getSessionTokenMaterial;

    public String execute(UUID authProcessUuid) {
        GetSessionTokenMaterial.Response sessionTokenMaterial =
            getSessionTokenMaterial.execute(
                new GetSessionTokenMaterial.Request(authProcessUuid)
            );

        return createSignedSdJwtForMid.execute(
            sessionTokenMaterial.unsignedJwt()
        );
    }
}
