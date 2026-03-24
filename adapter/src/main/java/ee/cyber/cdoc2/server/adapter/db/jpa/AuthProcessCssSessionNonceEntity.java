package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "AUTH_PROCESS_CSS_SESSION_NONCE")
@SequenceGenerator(
    name = "AUTH_PROCESS_CSS_SESSION_NONCE_PK_SEQUENCE",
    sequenceName = "AUTH_PROCESS_CSS_SESSION_NONCE_PK_SEQUENCE",
    allocationSize = 1
)
public class AuthProcessCssSessionNonceEntity {
    @Id
    @GeneratedValue(generator = "AUTH_PROCESS_CSS_SESSION_NONCE_PK_SEQUENCE")
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_process_id", nullable = false)
    private AuthProcessEntity authProcess;

    @Column(name = "css_uri", nullable = false)
    private String cssUri;

    @Column(name = "session_nonce", nullable = false)
    private String sessionNonce;
}
