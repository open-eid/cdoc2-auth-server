package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name = "AUTH_PROCESS_SESSION_NONCE")
@SequenceGenerator(
    name = "AUTH_PROCESS_SESSION_NONCE_PK_SEQUENCE",
    sequenceName = "AUTH_PROCESS_SESSION_NONCE_PK_SEQUENCE",
    allocationSize = 1
)
@ToString(exclude = {"authProcess"})
public class AuthProcessSessionNonceEntity {
    @Id
    @GeneratedValue(generator = "AUTH_PROCESS_SESSION_NONCE_PK_SEQUENCE")
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne()
    @JoinColumn(name = "auth_process_id", nullable = false)
    private AuthProcessEntity authProcess;

    @ManyToOne()
    @JoinColumn(name = "uri_id", nullable = false)
    private ServerSessionNonceUriEntity serverUri;

    @Column(name = "session_nonce", nullable = false)
    private String sessionNonce;
}
