package uk.co.Dunelm.loyalty.application.query;

import org.springframework.stereotype.Service;
import uk.co.Dunelm.loyalty.domain.model.PointsLedgerEntry;
import uk.co.Dunelm.loyalty.domain.model.Tier;
import uk.co.Dunelm.loyalty.domain.port.PointsLedgerRepository;
import uk.co.Dunelm.loyalty.domain.port.TierRepository;

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
                                    DunelmTierProgress DunelmTierProgress, List<PointsLedgerEntry> recentTransactions) {}
    public record TierInfo(String name, double multiplier, String badgeColor) {}
    public record DunelmTierProgress(String DunelmTierName, int pointsRequired, int pointsEarned, double progressPercent) {}

    public DashboardResponse execute(UUID customerId, int lifetimePoints) {
        var balance = getBalanceQuery.execute(customerId);
        var recent = ledgerRepository.findTop5ByCustomerIdOrderByCreatedAtDesc(customerId);

        var tiers = tierRepository.findAllByOrderBySortOrder();
        Tier currentTier = null;
        Tier DunelmTier = null;
        for (int i = 0; i < tiers.size(); i++) {
            if (lifetimePoints >= tiers.get(i).getThreshold()) {
                currentTier = tiers.get(i);
                if (i + 1 < tiers.size()) DunelmTier = tiers.get(i + 1);
            }
        }

        TierInfo tierInfo = currentTier != null
                ? new TierInfo(currentTier.getName(), currentTier.getMultiplier(), currentTier.getBadgeColor())
                : new TierInfo("Bronze", 1.0, "#CD7F32");

        DunelmTierProgress progress = DunelmTier != null
                ? new DunelmTierProgress(DunelmTier.getName(), DunelmTier.getThreshold(),
                    lifetimePoints, (double) lifetimePoints / DunelmTier.getThreshold() * 100)
                : null;

        return new DashboardResponse(balance, tierInfo, progress, recent);
    }
}
