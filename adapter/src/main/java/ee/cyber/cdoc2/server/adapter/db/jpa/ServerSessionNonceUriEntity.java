package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "SERVER_SESSION_NONCE_URI")
@SequenceGenerator(
    name = "SERVER_SESSION_NONCE_URI_PK_SEQUENCE",
    sequenceName = "SERVER_SESSION_NONCE_URI_PK_SEQUENCE",
    allocationSize = 1
)
public class ServerSessionNonceUriEntity {
    @Id
    @GeneratedValue(generator = "SERVER_SESSION_NONCE_URI_PK_SEQUENCE")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uri", nullable = false)
    private String uri;
}
