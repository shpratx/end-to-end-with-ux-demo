package co.uk.next.loyalty.notification.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "push_tokens")
public class PushToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 10)
    private String platform;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PushToken() {}

    public PushToken(UUID customerId, String platform, String token) {
        this.customerId = customerId;
        this.platform = platform;
        this.token = token;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getPlatform() { return platform; }
    public String getToken() { return token; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
}
