package uk.co.Dunelm.loyalty.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "points_ledger")
public class PointsLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false)
    private int points;

    @Column(name = "running_balance", nullable = false)
    private int balanceAfter;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum TransactionType {
        EARN, REDEEM, ADJUST, BONUS, EXPIRE, REVERSE
    }

    protected PointsLedgerEntry() {}

    public PointsLedgerEntry(UUID customerId, TransactionType type, int points, int balanceAfter,
                             String referenceId, String channel, UUID campaignId) {
        this.customerId = customerId;
        this.type = type;
        this.points = points;
        this.balanceAfter = balanceAfter;
        this.referenceId = referenceId;
        this.channel = channel;
        this.campaignId = campaignId;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public TransactionType getType() { return type; }
    public int getPoints() { return points; }
    public int getBalanceAfter() { return balanceAfter; }
    public String getReferenceId() { return referenceId; }
    public String getChannel() { return channel; }
    public UUID getCampaignId() { return campaignId; }
    public Instant getCreatedAt() { return createdAt; }
}
