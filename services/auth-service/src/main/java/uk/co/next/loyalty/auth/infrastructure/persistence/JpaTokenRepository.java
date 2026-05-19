package uk.co.next.loyalty.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.co.next.loyalty.auth.domain.model.RefreshToken;
import uk.co.next.loyalty.auth.domain.port.TokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTokenRepository extends JpaRepository<RefreshToken, UUID>, TokenRepository {

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    @Override
    default Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return findByTokenHashAndRevokedAtIsNull(tokenHash);
    }

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.customerId = :customerId AND r.revokedAt IS NULL")
    void revokeAllActive(UUID customerId, Instant now);

    @Override
    default void revokeAllByCustomerId(UUID customerId) {
        revokeAllActive(customerId, Instant.now());
    }
}
