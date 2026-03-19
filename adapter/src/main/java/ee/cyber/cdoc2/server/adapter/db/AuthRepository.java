package ee.cyber.cdoc2.server.adapter.db;

import java.util.HashMap;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import ee.cyber.cdoc2.server.app.usecase.GetAuthState;
import ee.cyber.cdoc2.server.app.usecase.StoreAuth;

@NullMarked
@Repository
public class AuthRepository implements StoreAuth, GetAuthState {
    private final HashMap<UUID, String> inMemoryDb = new HashMap<>();

    @Override
    public void execute(Request request) {
        inMemoryDb.put(request.authUuid(), request.authState());
    }

    @Override
    public String execute(UUID uuid) {
        return inMemoryDb.get(uuid);
    }
}
