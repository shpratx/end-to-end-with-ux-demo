package uk.co.next.loyalty.auth.domain.port;

import uk.co.next.loyalty.auth.domain.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void revokeAllByCustomerId(UUID customerId);
}
