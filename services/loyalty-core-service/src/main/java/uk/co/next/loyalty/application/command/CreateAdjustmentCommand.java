package uk.co.Dunelm.loyalty.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.Dunelm.loyalty.domain.model.Adjustment;
import uk.co.Dunelm.loyalty.domain.model.PointsLedgerEntry;
import uk.co.Dunelm.loyalty.domain.port.AdjustmentRepository;
import uk.co.Dunelm.loyalty.domain.port.PointsLedgerRepository;
import uk.co.Dunelm.loyalty.infrastructure.cache.RedisBalanceCache;

import java.util.UUID;

@Service
public class CreateAdjustmentCommand {

    private final AdjustmentRepository adjustmentRepository;
    private final PointsLedgerRepository ledgerRepository;
    private final RedisBalanceCache balanceCache;

    public CreateAdjustmentCommand(AdjustmentRepository adjustmentRepository,
                                   PointsLedgerRepository ledgerRepository,
                                   RedisBalanceCache balanceCache) {
        this.adjustmentRepository = adjustmentRepository;
        this.ledgerRepository = ledgerRepository;
        this.balanceCache = balanceCache;
    }

    public record Request(UUID customerId, UUID staffId, String action, int points, String reason, String notes) {}
    public record Response(UUID adjustmentId, String status, int pointsAdjusted, Integer newBalance) {}

    @Transactional
    public Response execute(Request request) {
        var action = Adjustment.AdjustmentAction.valueOf(request.action().toUpperCase());
        var adjustment = new Adjustment(request.customerId(), request.staffId(), action, request.points(), request.reason(), request.notes());
        adjustment = adjustmentRepository.save(adjustment);

        Integer newBalance = null;
        if (adjustment.getStatus() == Adjustment.AdjustmentStatus.APPROVED) {
            newBalance = applyAdjustment(request.customerId(), action, request.points());
        }

        return new Response(adjustment.getId(), adjustment.getStatus().name().toLowerCase(), request.points(), newBalance);
    }

    private int applyAdjustment(UUID customerId, Adjustment.AdjustmentAction action, int points) {
        int currentBalance = balanceCache.getBalance(customerId)
                .orElseGet(() -> ledgerRepository.sumPointsByCustomerId(customerId).orElse(0));
        int delta = action == Adjustment.AdjustmentAction.ADD ? points : -points;
        int newBalance = currentBalance + delta;

        var type = action == Adjustment.AdjustmentAction.ADD
                ? PointsLedgerEntry.TransactionType.ADJUST : PointsLedgerEntry.TransactionType.ADJUST;
        ledgerRepository.save(new PointsLedgerEntry(customerId, type, delta, newBalance, null, "system", null));
        balanceCache.setBalance(customerId, newBalance);
        return newBalance;
    }
}
