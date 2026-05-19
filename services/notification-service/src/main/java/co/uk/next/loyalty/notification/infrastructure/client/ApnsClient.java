package co.uk.Dunelm.loyalty.notification.infrastructure.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApnsClient {

    private static final Logger log = LoggerFactory.getLogger(ApnsClient.class);

    @CircuitBreaker(name = "apns")
    public void send(String deviceToken, String title, String body) {
        log.info("Sending APNs push to device: {}...", deviceToken.substring(0, Math.min(8, deviceToken.length())));
        // TODO: Integrate with APNs HTTP/2 client
        throw new UnsupportedOperationException("APNs integration pending");
    }
}
