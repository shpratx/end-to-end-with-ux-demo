package uk.co.next.loyalty.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_badges")
public class CustomerBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "badge_id", nullable = false)
    private UUID badgeId;

    @Column(name = "earned_at", nullable = false)
    private Instant earnedAt;

    protected CustomerBadge() {}

    public CustomerBadge(UUID customerId, UUID badgeId) {
        this.customerId = customerId;
        this.badgeId = badgeId;
        this.earnedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getBadgeId() { return badgeId; }
    public Instant getEarnedAt() { return earnedAt; }
}
