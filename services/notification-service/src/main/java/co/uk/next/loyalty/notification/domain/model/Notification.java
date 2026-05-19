package co.uk.Dunelm.loyalty.notification.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    protected Notification() {}

    public Notification(UUID customerId, String title, String body, String type, String channel) {
        this.customerId = customerId;
        this.title = title;
        this.body = body;
        this.type = type;
        this.channel = channel;
        this.createdAt = Instant.now();
    }

    public void markRead() {
        this.readAt = Instant.now();
    }

    public void markDelivered() {
        this.deliveredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getType() { return type; }
    public String getChannel() { return channel; }
    public Instant getReadAt() { return readAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isRead() { return readAt != null; }
}
