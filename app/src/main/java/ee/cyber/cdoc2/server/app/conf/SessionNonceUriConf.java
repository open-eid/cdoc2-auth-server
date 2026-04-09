package ee.cyber.cdoc2.server.app.conf;

import java.net.URI;
import java.util.List;

public abstract class SessionNonceUriConf {
    protected List<URI> uris;

    public List<URI> getUris() {
        return uris;
    }

    public abstract void reload();
}
