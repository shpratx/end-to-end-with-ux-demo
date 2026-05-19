package uk.co.next.loyalty.auth.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String version;

    private boolean accepted;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "is_deleted")
    private boolean deleted;

    protected Consent() {}

    public Consent(UUID customerId, String type, String version, boolean accepted, String ipAddress) {
        this.customerId = customerId;
        this.type = type;
        this.version = version;
        this.accepted = accepted;
        this.ipAddress = ipAddress;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getType() { return type; }
    public String getVersion() { return version; }
    public boolean isAccepted() { return accepted; }
}
