package uk.co.next.loyalty.domain.model;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignType type;

    @Column(nullable = false)
    private double value;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "eligibility", length = 30)
    private String eligibilityRules;

    @Column(name = "max_budget")
    private Integer maxBudget;

    @Column(name = "budget_used", nullable = false)
    private int spentBudget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status;

    public enum CampaignType { MULTIPLIER, FIXED_BONUS, CATEGORY }
    public enum CampaignStatus { DRAFT, ACTIVE, PAUSED, COMPLETED }

    protected Campaign() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public CampaignType getType() { return type; }
    public double getValue() { return value; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
    public String getEligibilityRules() { return eligibilityRules; }
    public Integer getMaxBudget() { return maxBudget; }
    public int getSpentBudget() { return spentBudget; }
    public CampaignStatus getStatus() { return status; }

    public boolean isWithinBudget(int points) {
        return maxBudget == null || (spentBudget + points) <= maxBudget;
    }

    public void addSpend(int points) {
        this.spentBudget += points;
    }
}
