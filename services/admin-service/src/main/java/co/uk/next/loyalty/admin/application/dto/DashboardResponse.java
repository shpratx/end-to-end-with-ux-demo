package co.uk.next.loyalty.admin.application.dto;

public record DashboardResponse(
        int activeMembers,
        int signUpsThisMonth,
        double redemptionRate,
        long totalPointsLiability,
        int activeCampaigns,
        int transactionsToday
) {}
