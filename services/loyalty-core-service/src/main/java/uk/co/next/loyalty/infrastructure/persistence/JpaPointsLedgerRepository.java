package uk.co.Dunelm.loyalty.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.co.Dunelm.loyalty.domain.model.PointsLedgerEntry;
import uk.co.Dunelm.loyalty.domain.port.PointsLedgerRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPointsLedgerRepository extends JpaRepository<PointsLedgerEntry, UUID>, PointsLedgerRepository {

    Optional<PointsLedgerEntry> findByReferenceId(String referenceId);

    Page<PointsLedgerEntry> findByCustomerId(UUID customerId, Pageable pageable);

    Page<PointsLedgerEntry> findByCustomerIdAndType(UUID customerId, PointsLedgerEntry.TransactionType type, Pageable pageable);

    Page<PointsLedgerEntry> findByCustomerIdAndCreatedAtBetween(UUID customerId, Instant from, Instant to, Pageable pageable);

    List<PointsLedgerEntry> findTop5ByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    @Query("SELECT SUM(p.points) FROM PointsLedgerEntry p WHERE p.customerId = :customerId")
    Optional<Integer> sumPointsByCustomerId(UUID customerId);
}
