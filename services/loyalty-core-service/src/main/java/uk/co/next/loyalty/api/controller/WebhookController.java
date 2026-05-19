package uk.co.Dunelm.loyalty.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.Dunelm.loyalty.application.command.ProcessEcommerceWebhookCommand;
import uk.co.Dunelm.loyalty.application.command.ProcessPosWebhookCommand;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final ProcessEcommerceWebhookCommand ecommerceWebhookCommand;
    private final ProcessPosWebhookCommand posWebhookCommand;

    public WebhookController(ProcessEcommerceWebhookCommand ecommerceWebhookCommand,
                             ProcessPosWebhookCommand posWebhookCommand) {
        this.ecommerceWebhookCommand = ecommerceWebhookCommand;
        this.posWebhookCommand = posWebhookCommand;
    }

    public record EcommerceWebhookRequest(String eventType, String orderId, String customerEmail, double totalAmount) {}
    public record PosWebhookRequest(String eventType, String transactionId, UUID customerId, double totalAmount, String storeId) {}

    @PostMapping("/ecommerce")
    public ResponseEntity<?> ecommerce(@RequestBody String rawBody,
                                       @RequestHeader("X-Webhook-Signature") String signature,
                                       @RequestHeader("Idempotency-Key") String idempotencyKey) {
        // Parse minimal fields from raw body for command
        var parsed = parseEcommerceBody(rawBody);
        var result = ecommerceWebhookCommand.execute(new ProcessEcommerceWebhookCommand.Request(
                parsed.eventType(), parsed.orderId(), parsed.customerId(), parsed.totalAmount(), signature, rawBody));
        return ResponseEntity.accepted().body(Map.of("accepted", result.accepted(), "correlationId", result.correlationId()));
    }

    @PostMapping("/pos")
    public ResponseEntity<?> pos(@RequestBody PosWebhookRequest body,
                                 @RequestHeader("Idempotency-Key") String idempotencyKey) {
        var result = posWebhookCommand.execute(new ProcessPosWebhookCommand.Request(
                body.eventType(), body.transactionId(), body.customerId(), body.totalAmount()));
        return ResponseEntity.accepted().body(Map.of("accepted", result.accepted(), "correlationId", result.correlationId()));
    }

    private record ParsedEcommerce(String eventType, String orderId, UUID customerId, double totalAmount) {}

    private ParsedEcommerce parseEcommerceBody(String rawBody) {
        // Simplified JSON parsing — in production use ObjectMapper
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            var node = mapper.readTree(rawBody);
            return new ParsedEcommerce(
                    node.path("eventType").asText(),
                    node.path("orderId").asText(),
                    UUID.fromString(node.path("customerId").asText(node.path("customerEmail").asText())),
                    node.path("totalAmount").asDouble());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid webhook body", e);
        }
    }
}
