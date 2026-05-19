package co.uk.Dunelm.loyalty.admin.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(name = "\"value\"", nullable = false)
    private double value;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(nullable = false)
    private String eligibility;

    @Column(name = "minimum_tier")
    private String minimumTier;

    @Column(name = "max_budget")
    private Integer maxBudget;

    @Column(name = "budget_used", nullable = false)
    private int budgetUsed;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }
    public String getMinimumTier() { return minimumTier; }
    public void setMinimumTier(String minimumTier) { this.minimumTier = minimumTier; }
    public Integer getMaxBudget() { return maxBudget; }
    public void setMaxBudget(Integer maxBudget) { this.maxBudget = maxBudget; }
    public int getBudgetUsed() { return budgetUsed; }
    public void setBudgetUsed(int budgetUsed) { this.budgetUsed = budgetUsed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
