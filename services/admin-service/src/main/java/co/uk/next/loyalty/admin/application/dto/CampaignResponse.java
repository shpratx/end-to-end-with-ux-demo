package co.uk.next.loyalty.admin.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(
        UUID campaignId,
        String name,
        String type,
        double value,
        Instant startDate,
        Instant endDate,
        String eligibility,
        String minimumTier,
        Integer maxBudget,
        int budgetUsed,
        String status,
        Instant createdAt
) {}
