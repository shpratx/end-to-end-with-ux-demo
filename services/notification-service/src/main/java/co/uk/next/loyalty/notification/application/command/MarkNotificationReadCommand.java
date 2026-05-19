package co.uk.Dunelm.loyalty.notification.application.command;

import co.uk.Dunelm.loyalty.notification.domain.model.Notification;
import co.uk.Dunelm.loyalty.notification.domain.port.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MarkNotificationReadCommand {

    private final NotificationRepository notificationRepository;

    public MarkNotificationReadCommand(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void execute(UUID notificationId, UUID customerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getCustomerId().equals(customerId)) {
            throw new SecurityException("Forbidden");
        }
        notification.markRead();
        notificationRepository.save(notification);
    }
}
