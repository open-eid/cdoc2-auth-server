package ee.cyber.cdoc2.server.app.usecase;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetStatusImpl implements GetStatus {
    private final GetAuthState getAuthState;

    @Override
    public String execute(String uuidStr) {
        return getAuthState.execute(UUID.fromString(uuidStr));
    }
}
