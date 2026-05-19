package co.uk.next.loyalty.notification.infrastructure.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FcmClient {

    private static final Logger log = LoggerFactory.getLogger(FcmClient.class);

    @CircuitBreaker(name = "fcm")
    public void send(String token, String title, String body) {
        log.info("Sending FCM push to token: {}...", token.substring(0, Math.min(8, token.length())));
        // TODO: Integrate with Firebase Admin SDK
        throw new UnsupportedOperationException("FCM integration pending");
    }
}
