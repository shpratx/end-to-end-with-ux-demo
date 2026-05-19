package uk.co.Dunelm.loyalty.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.Dunelm.loyalty.application.command.ApproveAdjustmentCommand;
import uk.co.Dunelm.loyalty.application.command.CreateAdjustmentCommand;
import uk.co.Dunelm.loyalty.application.query.GetLoyaltySummaryQuery;
import uk.co.Dunelm.loyalty.application.query.StaffCustomerSearchQuery;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffCustomerSearchQuery searchQuery;
    private final GetLoyaltySummaryQuery summaryQuery;
    private final CreateAdjustmentCommand createAdjustmentCommand;
    private final ApproveAdjustmentCommand approveAdjustmentCommand;

    public StaffController(StaffCustomerSearchQuery searchQuery,
                           GetLoyaltySummaryQuery summaryQuery,
                           CreateAdjustmentCommand createAdjustmentCommand,
                           ApproveAdjustmentCommand approveAdjustmentCommand) {
        this.searchQuery = searchQuery;
        this.summaryQuery = summaryQuery;
        this.createAdjustmentCommand = createAdjustmentCommand;
        this.approveAdjustmentCommand = approveAdjustmentCommand;
    }

    public record AdjustmentRequest(UUID customerId, String action, int points, String reason, String notes) {}
    public record ApprovalRequest(boolean approved, String reviewNotes) {}

    @GetMapping("/customers/search")
    public ResponseEntity<?> search(@RequestParam String q, @RequestParam String field) {
        return ResponseEntity.ok(Map.of("data", searchQuery.execute(q, field)));
    }

    @GetMapping("/customers/{id}/loyalty-summary")
    public ResponseEntity<?> loyaltySummary(@PathVariable UUID id) {
        return ResponseEntity.ok(summaryQuery.execute(id));
    }

    @PostMapping("/adjustments")
    public ResponseEntity<?> createAdjustment(@RequestBody AdjustmentRequest body,
                                              @RequestHeader("X-Staff-Id") UUID staffId) {
        var result = createAdjustmentCommand.execute(new CreateAdjustmentCommand.Request(
                body.customerId(), staffId, body.action(), body.points(), body.reason(), body.notes()));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/adjustments/{id}/approve")
    public ResponseEntity<?> approveAdjustment(@PathVariable UUID id,
                                               @RequestBody ApprovalRequest body,
                                               @RequestHeader("X-Staff-Id") UUID staffId) {
        var result = approveAdjustmentCommand.execute(new ApproveAdjustmentCommand.Request(id, staffId, body.approved()));
        return ResponseEntity.ok(result);
    }
}
