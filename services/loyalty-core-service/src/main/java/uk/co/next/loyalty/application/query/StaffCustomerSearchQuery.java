package uk.co.Dunelm.loyalty.application.query;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StaffCustomerSearchQuery {

    public record CustomerResult(UUID customerId, String name, String maskedEmail, String maskedPhone,
                                 String tier, int pointsBalance, String memberSince) {}

    // Placeholder — actual implementation queries customer table with PII masking
    public java.util.List<CustomerResult> execute(String query, String field) {
        return java.util.List.of();
    }
}
