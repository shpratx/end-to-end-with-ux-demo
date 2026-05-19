package uk.co.next.loyalty.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "adjustments")
public class Adjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "requested_by", nullable = false)
    private UUID staffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AdjustmentAction action;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column(length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdjustmentStatus status;

    @Column(name = "approved_by")
    private UUID approverId;

    @Column(name = "updated_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum AdjustmentAction { ADD, DEDUCT }
    public enum AdjustmentStatus { PENDING, APPROVED, REJECTED }

    protected Adjustment() {}

    public Adjustment(UUID customerId, UUID staffId, AdjustmentAction action, int points, String reason, String notes) {
        this.customerId = customerId;
        this.staffId = staffId;
        this.action = action;
        this.points = points;
        this.reason = reason;
        this.notes = notes;
        this.status = points > 500 ? AdjustmentStatus.PENDING : AdjustmentStatus.APPROVED;
        this.createdAt = Instant.now();
    }

    public void approve(UUID approverId) {
        this.status = AdjustmentStatus.APPROVED;
        this.approverId = approverId;
        this.approvedAt = Instant.now();
    }

    public void reject(UUID approverId) {
        this.status = AdjustmentStatus.REJECTED;
        this.approverId = approverId;
        this.approvedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getStaffId() { return staffId; }
    public AdjustmentAction getAction() { return action; }
    public int getPoints() { return points; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }
    public AdjustmentStatus getStatus() { return status; }
    public UUID getApproverId() { return approverId; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
