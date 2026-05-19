package uk.co.Dunelm.loyalty.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.Dunelm.loyalty.domain.model.Adjustment;
import uk.co.Dunelm.loyalty.domain.port.AdjustmentRepository;

import java.util.UUID;

@Repository
public interface JpaAdjustmentRepository extends JpaRepository<Adjustment, UUID>, AdjustmentRepository {
}
