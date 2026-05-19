package uk.co.Dunelm.loyalty.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.Dunelm.loyalty.application.query.GetCustomerTierQuery;
import uk.co.Dunelm.loyalty.application.query.GetTiersQuery;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tiers")
public class TierController {

    private final GetTiersQuery getTiersQuery;
    private final GetCustomerTierQuery getCustomerTierQuery;

    public TierController(GetTiersQuery getTiersQuery, GetCustomerTierQuery getCustomerTierQuery) {
        this.getTiersQuery = getTiersQuery;
        this.getCustomerTierQuery = getCustomerTierQuery;
    }

    @GetMapping
    public ResponseEntity<?> listTiers() {
        return ResponseEntity.ok(Map.of("data", getTiersQuery.execute()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> myTier(@RequestHeader(value = "X-Lifetime-Points", defaultValue = "0") int lifetimePoints) {
        return ResponseEntity.ok(getCustomerTierQuery.execute(lifetimePoints));
    }
}
