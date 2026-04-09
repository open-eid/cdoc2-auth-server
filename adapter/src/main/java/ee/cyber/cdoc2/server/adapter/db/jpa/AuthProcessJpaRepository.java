package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthProcessJpaRepository extends JpaRepository<AuthProcessEntity, Long> {
    AuthProcessEntity findByUuid(String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE AuthProcessEntity ap SET ap.status = :status WHERE ap.uuid = :uuid")
    int updateStatus(@Param("uuid") String uuid, @Param("status") String status);
}
