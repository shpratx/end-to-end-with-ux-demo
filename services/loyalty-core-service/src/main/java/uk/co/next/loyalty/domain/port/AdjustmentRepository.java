package uk.co.next.loyalty.domain.port;

import uk.co.next.loyalty.domain.model.Adjustment;
import java.util.Optional;
import java.util.UUID;

public interface AdjustmentRepository {
    Adjustment save(Adjustment adjustment);
    Optional<Adjustment> findById(UUID id);
}
