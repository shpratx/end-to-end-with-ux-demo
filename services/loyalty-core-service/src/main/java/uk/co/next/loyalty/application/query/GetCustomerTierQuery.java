package uk.co.Dunelm.loyalty.application.query;

import org.springframework.stereotype.Service;
import uk.co.Dunelm.loyalty.domain.model.Tier;
import uk.co.Dunelm.loyalty.domain.port.TierRepository;

import java.util.List;

@Service
public class GetCustomerTierQuery {

    private final TierRepository tierRepository;

    public GetCustomerTierQuery(TierRepository tierRepository) {
        this.tierRepository = tierRepository;
    }

    public record Response(Tier currentTier, int lifetimePoints, Integer pointsToDunelmTier, String DunelmTierName, double progressPercentage) {}

    public Response execute(int lifetimePoints) {
        var tiers = tierRepository.findAllByOrderBySortOrder();
        Tier current = null;
        Tier Dunelm = null;
        for (int i = 0; i < tiers.size(); i++) {
            if (lifetimePoints >= tiers.get(i).getThreshold()) {
                current = tiers.get(i);
                if (i + 1 < tiers.size()) Dunelm = tiers.get(i + 1);
            }
        }
        Integer pointsToDunelm = Dunelm != null ? Dunelm.getThreshold() - lifetimePoints : null;
        double progress = Dunelm != null ? (double) lifetimePoints / Dunelm.getThreshold() * 100 : 100.0;
        return new Response(current, lifetimePoints, pointsToDunelm, Dunelm != null ? Dunelm.getName() : null, progress);
    }
}
