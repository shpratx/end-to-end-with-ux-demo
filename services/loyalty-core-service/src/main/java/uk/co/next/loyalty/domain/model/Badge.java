package uk.co.next.loyalty.domain.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(nullable = false, length = 20)
    private String rarity;

    @Column(columnDefinition = "jsonb")
    private String criteria;

    protected Badge() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public String getRarity() { return rarity; }
    public String getCriteria() { return criteria; }
}
