package co.uk.Dunelm.loyalty.notification.domain.port;

import co.uk.Dunelm.loyalty.notification.domain.model.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PushTokenRepository extends JpaRepository<PushToken, UUID> {
    List<PushToken> findByCustomerIdAndActiveTrue(UUID customerId);
}
