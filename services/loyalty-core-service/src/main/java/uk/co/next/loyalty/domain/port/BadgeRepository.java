package uk.co.Dunelm.loyalty.domain.port;

import uk.co.Dunelm.loyalty.domain.model.Badge;
import uk.co.Dunelm.loyalty.domain.model.CustomerBadge;
import java.util.List;
import java.util.UUID;

public interface BadgeRepository {
    List<Badge> findAll();
    List<CustomerBadge> findByCustomerId(UUID customerId);
}
