package uk.co.Dunelm.loyalty.auth.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "social_accounts")
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "is_deleted")
    private boolean deleted;

    protected SocialAccount() {}

    public SocialAccount(UUID customerId, String provider, String providerUserId, String email) {
        this.customerId = customerId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getProvider() { return provider; }
    public String getProviderUserId() { return providerUserId; }
    public String getEmail() { return email; }
}
