package ee.cyber.cdoc2.server.app.usecase;


import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;
import ee.cyber.cdoc2.server.app.usecase.SessionNonce.UriSessionNonce;

@NullMarked
@RequiredArgsConstructor
@Component
public class StartAuthImpl implements StartAuth {
    private final StoreAuth storeAuth;
    private final SessionNonce sessionNonce;
    private final SessionNonceUriConf sessionNonceUriConf;

    @Override
    public UUID execute(Request request) {
        UUID authUuid = UUID.randomUUID();

        List<URI> sessionNonceUris = sessionNonceUriConf.getUris();
        List<UriSessionNonce> sessionNonces = sessionNonce.collectSessionNonces(sessionNonceUris);

        storeAuth.execute(new StoreAuth.Request(
            authUuid,
            UUID.randomUUID().toString(),
            sessionNonces
        ));

        return authUuid;
    }
}
