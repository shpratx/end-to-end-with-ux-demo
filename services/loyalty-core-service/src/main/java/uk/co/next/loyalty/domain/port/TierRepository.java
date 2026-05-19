package uk.co.next.loyalty.domain.port;

import uk.co.next.loyalty.domain.model.Tier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TierRepository {
    List<Tier> findAllByOrderBySortOrder();
    Optional<Tier> findById(UUID id);
    Optional<Tier> findTopByThresholdLessThanEqualOrderByThresholdDesc(int lifetimePoints);
}
