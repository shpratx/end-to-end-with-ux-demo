package co.uk.Dunelm.loyalty.notification.domain.port;

import co.uk.Dunelm.loyalty.notification.domain.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    Optional<NotificationTemplate> findByEventTypeAndActiveTrue(String eventType);
}
