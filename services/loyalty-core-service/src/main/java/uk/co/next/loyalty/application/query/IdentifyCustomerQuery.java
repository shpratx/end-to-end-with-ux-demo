package uk.co.next.loyalty.application.query;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdentifyCustomerQuery {

    public record Response(UUID customerId, String name, String tier, int pointsBalance, String memberSince) {}

    // Placeholder — actual implementation queries by phone/loyaltyId/qr
    public Response execute(String method, String value) {
        throw new IllegalArgumentException("CUSTOMER_NOT_FOUND");
    }
}
