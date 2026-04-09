package ee.cyber.cdoc2.server.adapter.conf;

import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;

public class SessionNonceUriConfImpl extends SessionNonceUriConf {
    private final SessionNonceUriDbCache sessionNonceUriDbCache;

    public SessionNonceUriConfImpl(
        SessionNonceUriDbCache sessionNonceUriDbCache
    ) {
        this.sessionNonceUriDbCache = sessionNonceUriDbCache;
        this.uris = this.sessionNonceUriDbCache.load();
    }

    @Override
    public void reload() {
        this.uris = sessionNonceUriDbCache.load();
    }
}
