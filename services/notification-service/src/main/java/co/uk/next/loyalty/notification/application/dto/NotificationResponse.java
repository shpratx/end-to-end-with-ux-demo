package co.uk.next.loyalty.notification.application.dto;

import co.uk.next.loyalty.notification.domain.model.Notification;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        String title,
        String body,
        String type,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitle(), n.getBody(), n.getType(), n.isRead(), n.getCreatedAt());
    }
}
