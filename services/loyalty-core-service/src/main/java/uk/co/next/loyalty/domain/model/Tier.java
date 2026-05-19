package uk.co.Dunelm.loyalty.domain.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tiers")
public class Tier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private int threshold;

    @Column(nullable = false)
    private double multiplier;

    @Column(name = "badge_color", length = 7)
    private String badgeColor;

    @Column(columnDefinition = "jsonb")
    private String benefits;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected Tier() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getThreshold() { return threshold; }
    public double getMultiplier() { return multiplier; }
    public String getBadgeColor() { return badgeColor; }
    public String getBenefits() { return benefits; }
    public int getSortOrder() { return sortOrder; }
}
