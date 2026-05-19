package uk.co.next.loyalty.application.query;

import org.springframework.stereotype.Service;
import uk.co.next.loyalty.domain.model.PointsLedgerEntry;
import uk.co.next.loyalty.domain.model.Tier;
import uk.co.next.loyalty.domain.port.PointsLedgerRepository;
import uk.co.next.loyalty.domain.port.TierRepository;

import java.util.List;
import java.util.UUID;

@Service
public class GetCustomerDashboardQuery {

    private final GetBalanceQuery getBalanceQuery;
    private final PointsLedgerRepository ledgerRepository;
    private final TierRepository tierRepository;

    public GetCustomerDashboardQuery(GetBalanceQuery getBalanceQuery,
                                     PointsLedgerRepository ledgerRepository,
                                     TierRepository tierRepository) {
        this.getBalanceQuery = getBalanceQuery;
        this.ledgerRepository = ledgerRepository;
        this.tierRepository = tierRepository;
    }

    public record DashboardResponse(GetBalanceQuery.Response balance, TierInfo tier,
                                    NextTierProgress nextTierProgress, List<PointsLedgerEntry> recentTransactions) {}
    public record TierInfo(String name, double multiplier, String badgeColor) {}
    public record NextTierProgress(String nextTierName, int pointsRequired, int pointsEarned, double progressPercent) {}

    public DashboardResponse execute(UUID customerId, int lifetimePoints) {
        var balance = getBalanceQuery.execute(customerId);
        var recent = ledgerRepository.findTop5ByCustomerIdOrderByCreatedAtDesc(customerId);

        var tiers = tierRepository.findAllByOrderBySortOrder();
        Tier currentTier = null;
        Tier nextTier = null;
        for (int i = 0; i < tiers.size(); i++) {
            if (lifetimePoints >= tiers.get(i).getThreshold()) {
                currentTier = tiers.get(i);
                if (i + 1 < tiers.size()) nextTier = tiers.get(i + 1);
            }
        }

        TierInfo tierInfo = currentTier != null
                ? new TierInfo(currentTier.getName(), currentTier.getMultiplier(), currentTier.getBadgeColor())
                : new TierInfo("Bronze", 1.0, "#CD7F32");

        NextTierProgress progress = nextTier != null
                ? new NextTierProgress(nextTier.getName(), nextTier.getThreshold(),
                    lifetimePoints, (double) lifetimePoints / nextTier.getThreshold() * 100)
                : null;

        return new DashboardResponse(balance, tierInfo, progress, recent);
    }
}
