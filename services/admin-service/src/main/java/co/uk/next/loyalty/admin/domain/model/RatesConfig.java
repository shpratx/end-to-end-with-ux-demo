package co.uk.Dunelm.loyalty.admin.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rates_config")
public class RatesConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "accrual_rate", nullable = false)
    private double accrualRate;

    @Column(name = "redemption_rate", nullable = false)
    private double redemptionRate;

    @Column(name = "minimum_redemption", nullable = false)
    private int minimumRedemption;

    @Column(name = "max_discount_percentage", nullable = false)
    private double maxDiscountPercentage;

    @Column(name = "expiration_months", nullable = false)
    private int expirationMonths;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public double getAccrualRate() { return accrualRate; }
    public void setAccrualRate(double accrualRate) { this.accrualRate = accrualRate; }
    public double getRedemptionRate() { return redemptionRate; }
    public void setRedemptionRate(double redemptionRate) { this.redemptionRate = redemptionRate; }
    public int getMinimumRedemption() { return minimumRedemption; }
    public void setMinimumRedemption(int minimumRedemption) { this.minimumRedemption = minimumRedemption; }
    public double getMaxDiscountPercentage() { return maxDiscountPercentage; }
    public void setMaxDiscountPercentage(double maxDiscountPercentage) { this.maxDiscountPercentage = maxDiscountPercentage; }
    public int getExpirationMonths() { return expirationMonths; }
    public void setExpirationMonths(int expirationMonths) { this.expirationMonths = expirationMonths; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
