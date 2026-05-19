package uk.co.next.loyalty.domain.port;

import uk.co.next.loyalty.domain.model.PointsLedgerEntry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointsLedgerRepository {
    PointsLedgerEntry save(PointsLedgerEntry entry);
    Optional<PointsLedgerEntry> findByReferenceId(String referenceId);
    Page<PointsLedgerEntry> findByCustomerId(UUID customerId, Pageable pageable);
    Page<PointsLedgerEntry> findByCustomerIdAndType(UUID customerId, PointsLedgerEntry.TransactionType type, Pageable pageable);
    Page<PointsLedgerEntry> findByCustomerIdAndCreatedAtBetween(UUID customerId, Instant from, Instant to, Pageable pageable);
    List<PointsLedgerEntry> findTop5ByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<Integer> sumPointsByCustomerId(UUID customerId);
}
