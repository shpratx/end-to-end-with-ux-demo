package uk.co.next.loyalty.domain.port;

import uk.co.next.loyalty.domain.model.Campaign;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CampaignRepository {
    List<Campaign> findActiveAt(Instant now);
    Campaign save(Campaign campaign);
}
