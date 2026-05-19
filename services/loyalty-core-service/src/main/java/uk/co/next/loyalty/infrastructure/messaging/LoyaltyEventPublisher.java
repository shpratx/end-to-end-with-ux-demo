package uk.co.Dunelm.loyalty.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class LoyaltyEventPublisher {

    private static final String TOPIC = "loyalty-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LoyaltyEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPointsEarned(UUID customerId, int points, int newBalance) {
        kafkaTemplate.send(TOPIC, customerId.toString(),
                Map.of("event", "points.earned", "customerId", customerId, "points", points, "newBalance", newBalance));
    }

    public void publishPointsRedeemed(UUID customerId, int points, int newBalance) {
        kafkaTemplate.send(TOPIC, customerId.toString(),
                Map.of("event", "points.redeemed", "customerId", customerId, "points", points, "newBalance", newBalance));
    }

    public void publishTierChanged(UUID customerId, String oldTier, String newTier) {
        kafkaTemplate.send(TOPIC, customerId.toString(),
                Map.of("event", "tier.changed", "customerId", customerId, "oldTier", oldTier, "newTier", newTier));
    }
}
