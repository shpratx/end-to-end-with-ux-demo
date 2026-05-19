package uk.co.next.loyalty.auth.domain.port;

import uk.co.next.loyalty.auth.domain.model.OtpCode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository {
    OtpCode save(OtpCode otpCode);
    Optional<OtpCode> findLatestUnusedByCustomerId(UUID customerId);
    long countByCustomerIdSince(UUID customerId, Instant since);
}
