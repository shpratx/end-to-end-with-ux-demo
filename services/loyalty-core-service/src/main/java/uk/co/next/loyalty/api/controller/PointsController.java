package uk.co.next.loyalty.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.next.loyalty.application.command.EarnPointsCommand;
import uk.co.next.loyalty.application.command.RedeemPointsCommand;
import uk.co.next.loyalty.application.query.GetBalanceQuery;
import uk.co.next.loyalty.application.query.GetTransactionsQuery;
import uk.co.next.loyalty.domain.model.PointsLedgerEntry;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/points")
public class PointsController {

    private final EarnPointsCommand earnPointsCommand;
    private final RedeemPointsCommand redeemPointsCommand;
    private final GetBalanceQuery getBalanceQuery;
    private final GetTransactionsQuery getTransactionsQuery;

    public PointsController(EarnPointsCommand earnPointsCommand,
                            RedeemPointsCommand redeemPointsCommand,
                            GetBalanceQuery getBalanceQuery,
                            GetTransactionsQuery getTransactionsQuery) {
        this.earnPointsCommand = earnPointsCommand;
        this.redeemPointsCommand = redeemPointsCommand;
        this.getBalanceQuery = getBalanceQuery;
        this.getTransactionsQuery = getTransactionsQuery;
    }

    public record EarnRequest(UUID customerId, double transactionAmount, String referenceId, String channel) {}
    public record RedeemRequest(int pointsToRedeem, String orderId, double orderTotal, String channel) {}

    @PostMapping("/earn")
    public ResponseEntity<?> earn(@RequestBody EarnRequest body,
                                  @RequestHeader("Idempotency-Key") String idempotencyKey) {
        var result = earnPointsCommand.execute(new EarnPointsCommand.Request(
                body.customerId(), body.transactionAmount(), body.referenceId(), body.channel()));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeem(@RequestBody RedeemRequest body,
                                    @RequestHeader(value = "X-Customer-Id") UUID customerId,
                                    @RequestHeader("Idempotency-Key") String idempotencyKey) {
        var result = redeemPointsCommand.execute(new RedeemPointsCommand.Request(
                customerId, body.pointsToRedeem(), body.orderId(), body.orderTotal(), body.channel()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> balance(@RequestHeader("X-Customer-Id") UUID customerId) {
        return ResponseEntity.ok(getBalanceQuery.execute(customerId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> transactions(@RequestHeader("X-Customer-Id") UUID customerId,
                                          @RequestParam(defaultValue = "1") int pageNumber,
                                          @RequestParam(defaultValue = "20") int pageSize,
                                          @RequestParam(required = false) String type) {
        Page<PointsLedgerEntry> page = getTransactionsQuery.execute(customerId, type, pageNumber, pageSize);
        return ResponseEntity.ok(Map.of(
                "data", page.getContent(),
                "meta", Map.of(
                        "pageNumber", page.getNumber() + 1,
                        "pageSize", page.getSize(),
                        "totalItems", page.getTotalElements(),
                        "totalPages", page.getTotalPages(),
                        "hasNextPage", page.hasNext(),
                        "hasPreviousPage", page.hasPrevious()
                )
        ));
    }
}
