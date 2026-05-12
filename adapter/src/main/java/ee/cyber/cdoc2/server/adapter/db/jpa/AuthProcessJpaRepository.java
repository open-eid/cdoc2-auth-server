package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.transaction.Transactional;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ee.cyber.cdoc2.server.adapter.db.jpa.projection.AuthProcessSessionTokenMaterial;
import ee.cyber.cdoc2.server.adapter.db.jpa.projection.AuthProcessStatusMidSidSession;

public interface AuthProcessJpaRepository extends JpaRepository<AuthProcessEntity, Long> {
    AuthProcessStatusMidSidSession findStatusMidSidSessionByUuid(String uuid);

    AuthProcessSessionTokenMaterial findSessionTokenMaterialByUuid(String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE AuthProcessEntity ap SET ap.status = :status, ap.endResult = :endResult "
        + "WHERE ap.uuid = :uuid")
    int updateStatus(
        @Param("uuid") String uuid,
        @Param("status") String status,
        @Param("endResult") String endResult
    );

    @Modifying
    @Transactional
    @Query("UPDATE AuthProcessEntity ap SET ap.status = :status, ap.endResult = :endResult,"
        + "ap.sessionToken = :sessionToken, ap.signingCert = :signingCert "
        + "WHERE ap.uuid = :uuid")
    int updateStatus(
        @Param("uuid") String uuid,
        @Param("status") String status,
        @Param("endResult") String endResult,
        @Param("sessionToken") String sessionToken,
        @Param("signingCert") String signingCert
    );

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM auth_process WHERE id IN "
        + "(SELECT id FROM auth_process "
        + "WHERE created_at < :createdAtCutoff LIMIT :deletionLimit)",
        nativeQuery = true)
    int deleteExpiredAuthProcesses(
        @Param("createdAtCutoff") Instant createdAtCutoff,
        @Param("deletionLimit") int deletionLimit
    );
}
