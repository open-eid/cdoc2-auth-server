package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@Table(name = "AUTH_PROCESS")
@SequenceGenerator(
    name = "AUTH_PROCESS_PK_SEQUENCE",
    sequenceName = "AUTH_PROCESS_PK_SEQUENCE",
    allocationSize = 1
)
public class AuthProcessEntity {
    @Id
    @GeneratedValue(generator = "AUTH_PROCESS_PK_SEQUENCE")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "interactions_digest", nullable = false)
    private String interactionsDigest;

    @Column(name = "rp_challenge", nullable = false)
    private String rpChallenge;

    @Column(name = "end_result")
    private String endResult;

    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Column(name = "mid_sid_session_id", nullable = false)
    private String midSidSessionId;

    @Column(name = "unsigned_sdjwt", nullable = false)
    private String unsignedSdJwt;

    @Column(name = "session_token")
    private String sessionToken;

    @Column(name = "signing_cert")
    private String signingCert;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
