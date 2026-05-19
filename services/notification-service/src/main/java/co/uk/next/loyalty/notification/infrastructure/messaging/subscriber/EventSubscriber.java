package co.uk.Dunelm.loyalty.notification.infrastructure.messaging.subscriber;

import co.uk.Dunelm.loyalty.notification.application.command.SendNotificationCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(EventSubscriber.class);
    private final SendNotificationCommand sendNotificationCommand;
    private final ObjectMapper objectMapper;

    public EventSubscriber(SendNotificationCommand sendNotificationCommand, ObjectMapper objectMapper) {
        this.sendNotificationCommand = sendNotificationCommand;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "customer-events", groupId = "notification-service")
    public void handleCustomerEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.get("eventType").asText();
            if ("customer.registered".equals(eventType)) {
                UUID customerId = UUID.fromString(node.get("customer_id").asText());
                Map<String, String> variables = Map.of("email", node.path("email").asText(""));
                sendNotificationCommand.execute(new SendNotificationCommand.Input(
                        customerId, "customer.registered", variables, List.of("email", "in_app")));
            }
        } catch (Exception e) {
            log.error("Failed to process customer event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "notification-commands", groupId = "notification-service")
    public void handleNotificationCommand(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            UUID customerId = UUID.fromString(node.get("customer_id").asText());
            String templateId = node.get("template_id").asText();
            Map<String, String> variables = new HashMap<>();
            node.path("variables").fields().forEachRemaining(e -> variables.put(e.getKey(), e.getValue().asText()));
            List<String> channels = new ArrayList<>();
            node.path("channels").forEach(c -> channels.add(c.asText()));

            sendNotificationCommand.execute(new SendNotificationCommand.Input(customerId, templateId, variables, channels));
        } catch (Exception e) {
            log.error("Failed to process notification command: {}", e.getMessage(), e);
        }
    }
}
