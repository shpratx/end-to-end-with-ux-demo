package co.uk.next.loyalty.notification.infrastructure.messaging.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventPublisher {

    private static final String TOPIC = "notification-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDelivered(UUID notificationId, String channel) {
        kafkaTemplate.send(TOPIC, notificationId.toString(), Map.of(
                "eventType", "notification.delivered",
                "notification_id", notificationId.toString(),
                "channel", channel,
                "timestamp", Instant.now().toString()
        ));
    }

    public void publishFailed(UUID notificationId, String channel, String reason, int retryCount) {
        kafkaTemplate.send(TOPIC, notificationId.toString(), Map.of(
                "eventType", "notification.failed",
                "notification_id", notificationId.toString(),
                "channel", channel,
                "reason", reason,
                "retry_count", retryCount,
                "timestamp", Instant.now().toString()
        ));
    }
}
