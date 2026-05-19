package uk.co.next.loyalty.application.query;

import org.springframework.stereotype.Service;
import uk.co.next.loyalty.domain.model.Tier;
import uk.co.next.loyalty.domain.port.TierRepository;

import java.util.List;

@Service
public class GetCustomerTierQuery {

    private final TierRepository tierRepository;

    public GetCustomerTierQuery(TierRepository tierRepository) {
        this.tierRepository = tierRepository;
    }

    public record Response(Tier currentTier, int lifetimePoints, Integer pointsToNextTier, String nextTierName, double progressPercentage) {}

    public Response execute(int lifetimePoints) {
        var tiers = tierRepository.findAllByOrderBySortOrder();
        Tier current = null;
        Tier next = null;
        for (int i = 0; i < tiers.size(); i++) {
            if (lifetimePoints >= tiers.get(i).getThreshold()) {
                current = tiers.get(i);
                if (i + 1 < tiers.size()) next = tiers.get(i + 1);
            }
        }
        Integer pointsToNext = next != null ? next.getThreshold() - lifetimePoints : null;
        double progress = next != null ? (double) lifetimePoints / next.getThreshold() * 100 : 100.0;
        return new Response(current, lifetimePoints, pointsToNext, next != null ? next.getName() : null, progress);
    }
}
