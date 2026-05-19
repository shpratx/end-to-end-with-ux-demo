package uk.co.Dunelm.loyalty.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.Dunelm.loyalty.domain.model.Tier;
import uk.co.Dunelm.loyalty.domain.port.TierRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTierRepository extends JpaRepository<Tier, UUID>, TierRepository {
    List<Tier> findAllByOrderBySortOrder();
    Optional<Tier> findTopByThresholdLessThanEqualOrderByThresholdDesc(int lifetimePoints);
}
