package co.uk.Dunelm.loyalty.notification.application.command;

import co.uk.Dunelm.loyalty.notification.domain.model.Notification;
import co.uk.Dunelm.loyalty.notification.domain.model.NotificationTemplate;
import co.uk.Dunelm.loyalty.notification.domain.model.PushToken;
import co.uk.Dunelm.loyalty.notification.domain.port.NotificationRepository;
import co.uk.Dunelm.loyalty.notification.domain.port.PushTokenRepository;
import co.uk.Dunelm.loyalty.notification.domain.port.TemplateRepository;
import co.uk.Dunelm.loyalty.notification.infrastructure.client.FcmClient;
import co.uk.Dunelm.loyalty.notification.infrastructure.client.ApnsClient;
import co.uk.Dunelm.loyalty.notification.infrastructure.client.EmailClient;
import co.uk.Dunelm.loyalty.notification.infrastructure.messaging.publisher.NotificationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SendNotificationCommand {

    private static final Logger log = LoggerFactory.getLogger(SendNotificationCommand.class);
    private static final int MAX_RETRIES = 3;

    private final TemplateRepository templateRepository;
    private final NotificationRepository notificationRepository;
    private final PushTokenRepository pushTokenRepository;
    private final FcmClient fcmClient;
    private final ApnsClient apnsClient;
    private final EmailClient emailClient;
    private final NotificationEventPublisher eventPublisher;
    private long[] backoffMs = {1000L, 5000L, 30000L};

    public SendNotificationCommand(TemplateRepository templateRepository,
                                   NotificationRepository notificationRepository,
                                   PushTokenRepository pushTokenRepository,
                                   FcmClient fcmClient,
                                   ApnsClient apnsClient,
                                   EmailClient emailClient,
                                   NotificationEventPublisher eventPublisher) {
        this.templateRepository = templateRepository;
        this.notificationRepository = notificationRepository;
        this.pushTokenRepository = pushTokenRepository;
        this.fcmClient = fcmClient;
        this.apnsClient = apnsClient;
        this.emailClient = emailClient;
        this.eventPublisher = eventPublisher;
    }

    /** For testing only — override backoff delays */
    void setBackoffMs(long[] backoffMs) {
        this.backoffMs = backoffMs;
    }

    public record Input(UUID customerId, String templateId, Map<String, String> variables, List<String> channels) {}

    public void execute(Input input) {
        NotificationTemplate template = templateRepository.findByEventTypeAndActiveTrue(input.templateId())
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + input.templateId()));

        String title = template.renderTitle(input.variables());
        String body = template.renderBody(input.variables());
        List<String> channels = input.channels() != null && !input.channels().isEmpty()
                ? input.channels()
                : Arrays.asList(template.getChannels().split(","));

        List<String> deliveredChannels = new ArrayList<>();

        for (String channel : channels) {
            Notification notification = new Notification(input.customerId(), title, body, "transactional", channel.trim());
            boolean delivered = deliverWithRetry(input.customerId(), title, body, channel.trim());

            if (!delivered && "push".equals(channel.trim())) {
                // Fallback: push fails → email
                delivered = deliverWithRetry(input.customerId(), title, body, "email");
                notification = new Notification(input.customerId(), title, body, "transactional", "email");
                if (delivered) deliveredChannels.add("email");
            } else if (delivered) {
                deliveredChannels.add(channel.trim());
            }

            if (delivered) {
                notification.markDelivered();
            }
            notificationRepository.save(notification);

            if (!delivered) {
                eventPublisher.publishFailed(notification.getId(), channel.trim(), "Delivery failed after retries", MAX_RETRIES);
            }
        }

        if (!deliveredChannels.isEmpty()) {
            Notification saved = notificationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(
                    input.customerId(), org.springframework.data.domain.PageRequest.of(0, 1)).getContent().get(0);
            eventPublisher.publishDelivered(saved.getId(), deliveredChannels.get(0));
        }
    }

    private boolean deliverWithRetry(UUID customerId, String title, String body, String channel) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                switch (channel) {
                    case "push" -> sendPush(customerId, title, body);
                    case "email" -> emailClient.send(customerId, title, body);
                    case "in_app" -> { return true; } // in-app is just DB insert
                    default -> { return false; }
                }
                return true;
            } catch (Exception e) {
                log.warn("Delivery attempt {} failed for channel {}: {}", attempt + 1, channel, e.getMessage());
                if (attempt < MAX_RETRIES - 1) {
                    try { Thread.sleep(backoffMs[attempt]); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return false; }
                }
            }
        }
        return false;
    }

    private void sendPush(UUID customerId, String title, String body) {
        List<PushToken> tokens = pushTokenRepository.findByCustomerIdAndActiveTrue(customerId);
        if (tokens.isEmpty()) throw new RuntimeException("No active push tokens");
        for (PushToken token : tokens) {
            if ("ios".equals(token.getPlatform())) {
                apnsClient.send(token.getToken(), title, body);
            } else {
                fcmClient.send(token.getToken(), title, body);
            }
        }
    }
}
