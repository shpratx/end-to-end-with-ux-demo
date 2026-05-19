package co.uk.next.loyalty.admin.application.dto;

import java.time.Instant;

public record RatesConfigResponse(
        double accrualRate,
        double redemptionRate,
        int minimumRedemption,
        double maxDiscountPercentage,
        int expirationMonths,
        Instant updatedAt
) {}
