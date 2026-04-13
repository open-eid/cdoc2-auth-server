package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ee.cyber.cdoc2.server.adapter.db.jpa.projection.AuthProcessStatusMidSidSession;
import ee.cyber.cdoc2.server.adapter.db.jpa.projection.AuthProcessUnsignedSdJwt;

public interface AuthProcessJpaRepository extends JpaRepository<AuthProcessEntity, Long> {
    AuthProcessStatusMidSidSession findStatusMidSidSessionByUuid(String uuid);

    AuthProcessUnsignedSdJwt findUnsignedJwtByUuid(String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE AuthProcessEntity ap SET ap.status = :status, ap.endResult = :endResult "
        + "WHERE ap.uuid = :uuid")
    int updateStatus(
        @Param("uuid") String uuid,
        @Param("status") String status,
        @Param("endResult") String endResult
    );
}
