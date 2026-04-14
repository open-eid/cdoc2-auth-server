package ee.cyber.cdoc2.server.adapter.db.jpa.projection;

public interface AuthProcessSessionTokenMaterial {
    String getUnsignedSdJwt();

    String getInteractionsDigest();

    String getRpChallenge();
}
