package uk.co.next.loyalty.auth.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.PENDING_VERIFICATION;

    @Column(name = "loyalty_id", nullable = false, unique = true)
    private String loyaltyId;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "phone_verified")
    private boolean phoneVerified;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "is_deleted")
    private boolean deleted;

    public enum CustomerStatus {
        PENDING_VERIFICATION, ACTIVE, SUSPENDED, DELETED
    }

    protected Customer() {}

    public Customer(String email, String phone, String name, String passwordHash, String loyaltyId) {
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.passwordHash = passwordHash;
        this.loyaltyId = loyaltyId;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
    public CustomerStatus getStatus() { return status; }
    public String getLoyaltyId() { return loyaltyId; }
    public boolean isEmailVerified() { return emailVerified; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }
    public boolean isDeleted() { return deleted; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void activate() {
        this.status = CustomerStatus.ACTIVE;
        this.emailVerified = true;
        this.updatedAt = Instant.now();
    }

    public void setPasswordHash(String hash) {
        this.passwordHash = hash;
        this.updatedAt = Instant.now();
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = Instant.now().plusSeconds(1800);
        }
        this.updatedAt = Instant.now();
    }

    public void resetFailedLogins() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = Instant.now();
    }

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }
}
