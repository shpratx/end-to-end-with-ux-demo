package co.uk.next.loyalty.notification.infrastructure.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmailClient {

    private static final Logger log = LoggerFactory.getLogger(EmailClient.class);

    @CircuitBreaker(name = "email")
    public void send(UUID customerId, String subject, String body) {
        log.info("Sending email to customer: {}", customerId);
        // TODO: Integrate with SendGrid/SES
        throw new UnsupportedOperationException("Email integration pending");
    }
}
