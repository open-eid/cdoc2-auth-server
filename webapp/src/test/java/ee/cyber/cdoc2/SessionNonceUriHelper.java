package ee.cyber.cdoc2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceJpaRepository;
import ee.cyber.cdoc2.server.adapter.db.jpa.ServerSessionNonceUriEntity;
import ee.cyber.cdoc2.server.app.conf.SessionNonceUriConf;

@Component
public class SessionNonceUriHelper {
    private static final String SERVER_URI = "http://localhost:8080";
    public static final String SESSION_NONCE_URI_1 = "/session_nonce_1";
    public static final String SESSION_NONCE_URI_2 = "/session_nonce_2";

    @Autowired
    private ServerSessionNonceJpaRepository serverSessionNonceJpaRepository;
    @Autowired
    private SessionNonceUriConf sessionNonceUriConf;

    void createSessionNonceUriTestData() {
        serverSessionNonceJpaRepository.deleteAll();

        ServerSessionNonceUriEntity entity = new ServerSessionNonceUriEntity();
        entity.setUri(SERVER_URI + SESSION_NONCE_URI_1);
        serverSessionNonceJpaRepository.save(entity);
        entity = new ServerSessionNonceUriEntity();
        entity.setUri(SERVER_URI + SESSION_NONCE_URI_2);
        serverSessionNonceJpaRepository.save(entity);

        sessionNonceUriConf.reload();
    }
}
