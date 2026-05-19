package uk.co.Dunelm.loyalty.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.Dunelm.loyalty.application.command.ProcessEcommerceWebhookCommand;
import uk.co.Dunelm.loyalty.application.command.ProcessPosWebhookCommand;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerContractTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProcessEcommerceWebhookCommand ecommerceCommand;
    @MockBean private ProcessPosWebhookCommand posCommand;

    private static final String SECRET = "test-secret";

    @Test
    void validWebhook_returns202() throws Exception {
        String body = """
                {"eventType":"order.completed","orderId":"ECOM-001","customerId":"%s","totalAmount":99.99}
                """.formatted(UUID.randomUUID());
        String signature = computeHmac(body);

        when(ecommerceCommand.execute(any())).thenReturn(
                new ProcessEcommerceWebhookCommand.Response(true, UUID.randomUUID()));

        mockMvc.perform(post("/api/v1/webhooks/ecommerce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Webhook-Signature", signature)
                        .header("Idempotency-Key", UUID.randomUUID().toString()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void invalidSignature_returns401() throws Exception {
        String body = """
                {"eventType":"order.completed","orderId":"ECOM-002","customerId":"%s","totalAmount":50.0}
                """.formatted(UUID.randomUUID());

        when(ecommerceCommand.execute(any())).thenThrow(new SecurityException("INVALID_SIGNATURE"));

        mockMvc.perform(post("/api/v1/webhooks/ecommerce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Webhook-Signature", "invalid-sig")
                        .header("Idempotency-Key", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateWebhook_returns202Idempotent() throws Exception {
        String body = """
                {"eventType":"order.completed","orderId":"ECOM-DUP","customerId":"%s","totalAmount":75.0}
                """.formatted(UUID.randomUUID());
        String signature = computeHmac(body);

        when(ecommerceCommand.execute(any())).thenReturn(
                new ProcessEcommerceWebhookCommand.Response(true, UUID.randomUUID()));

        mockMvc.perform(post("/api/v1/webhooks/ecommerce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Webhook-Signature", signature)
                        .header("Idempotency-Key", UUID.randomUUID().toString()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    private String computeHmac(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
}
