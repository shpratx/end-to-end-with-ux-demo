package co.uk.Dunelm.loyalty.notification.application.query;

import co.uk.Dunelm.loyalty.notification.domain.port.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetUnreadCountQuery {

    private final NotificationRepository notificationRepository;

    public GetUnreadCountQuery(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public long execute(UUID customerId) {
        return notificationRepository.countUnreadByCustomerId(customerId);
    }
}
