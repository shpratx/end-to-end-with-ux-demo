package uk.co.next.loyalty.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.next.loyalty.application.query.GetCustomerDashboardQuery;
import uk.co.next.loyalty.application.query.GetCustomerQrCodeQuery;
import uk.co.next.loyalty.application.query.IdentifyCustomerQuery;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final GetCustomerDashboardQuery dashboardQuery;
    private final GetCustomerQrCodeQuery qrCodeQuery;
    private final IdentifyCustomerQuery identifyQuery;

    public CustomerController(GetCustomerDashboardQuery dashboardQuery,
                              GetCustomerQrCodeQuery qrCodeQuery,
                              IdentifyCustomerQuery identifyQuery) {
        this.dashboardQuery = dashboardQuery;
        this.qrCodeQuery = qrCodeQuery;
        this.identifyQuery = identifyQuery;
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<?> dashboard(@RequestHeader("X-Customer-Id") UUID customerId,
                                       @RequestHeader(value = "X-Lifetime-Points", defaultValue = "0") int lifetimePoints) {
        return ResponseEntity.ok(dashboardQuery.execute(customerId, lifetimePoints));
    }

    @GetMapping("/me/qr-code")
    public ResponseEntity<?> qrCode(@RequestHeader("X-Customer-Id") UUID customerId) {
        return ResponseEntity.ok(qrCodeQuery.execute(customerId));
    }

    @GetMapping("/identify")
    public ResponseEntity<?> identify(@RequestParam String method, @RequestParam String value) {
        return ResponseEntity.ok(identifyQuery.execute(method, value));
    }
}
