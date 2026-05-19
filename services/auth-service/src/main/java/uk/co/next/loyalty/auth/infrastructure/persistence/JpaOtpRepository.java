package uk.co.next.loyalty.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.co.next.loyalty.auth.domain.model.OtpCode;
import uk.co.next.loyalty.auth.domain.port.OtpRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaOtpRepository extends JpaRepository<OtpCode, UUID>, OtpRepository {

    @Query("SELECT o FROM OtpCode o WHERE o.customerId = :customerId AND o.usedAt IS NULL ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpCode> findLatestUnusedByCustomerId(UUID customerId);

    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.customerId = :customerId AND o.createdAt >= :since")
    long countByCustomerIdSince(UUID customerId, Instant since);
}
