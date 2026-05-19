package uk.co.next.loyalty.application.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.next.loyalty.domain.port.PointsLedgerRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ProcessEcommerceWebhookCommand {

    private final EarnPointsCommand earnPointsCommand;
    private final PointsLedgerRepository ledgerRepository;

    @Value("${loyalty.webhook.ecommerce-secret:default-secret}")
    private String webhookSecret;

    public ProcessEcommerceWebhookCommand(EarnPointsCommand earnPointsCommand,
                                          PointsLedgerRepository ledgerRepository) {
        this.earnPointsCommand = earnPointsCommand;
        this.ledgerRepository = ledgerRepository;
    }

    public record Request(String eventType, String orderId, UUID customerId, double totalAmount, String signature, String rawBody) {}
    public record Response(boolean accepted, UUID correlationId) {}

    public Response execute(Request request) {
        if (!verifySignature(request.rawBody(), request.signature())) {
            throw new SecurityException("INVALID_SIGNATURE");
        }

        // Dedup by orderId+eventType
        String dedupKey = request.orderId() + ":" + request.eventType();
        if (ledgerRepository.findByReferenceId(dedupKey).isPresent()) {
            return new Response(true, UUID.randomUUID()); // idempotent
        }

        if ("order.completed".equals(request.eventType())) {
            earnPointsCommand.execute(new EarnPointsCommand.Request(
                    request.customerId(), request.totalAmount(), dedupKey, "online"));
        }
        // order.refunded handled as REVERSE type — simplified

        return new Response(true, UUID.randomUUID());
    }

    private boolean verifySignature(String body, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String computed = HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
