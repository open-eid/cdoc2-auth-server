package ee.cyber.cdoc2.server.adapter.db.jpa.projection;

public interface AuthProcessStatusMidSidSession {
    String getStatus();

    String getEndResult();

    String getMidSidSessionId();

    String getSessionToken();

    String getSigningCert();
}
