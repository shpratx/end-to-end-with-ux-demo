package uk.co.next.loyalty.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.next.loyalty.domain.model.Adjustment;
import uk.co.next.loyalty.domain.model.PointsLedgerEntry;
import uk.co.next.loyalty.domain.port.AdjustmentRepository;
import uk.co.next.loyalty.domain.port.PointsLedgerRepository;
import uk.co.next.loyalty.infrastructure.cache.RedisBalanceCache;

import java.util.UUID;

@Service
public class ApproveAdjustmentCommand {

    private final AdjustmentRepository adjustmentRepository;
    private final PointsLedgerRepository ledgerRepository;
    private final RedisBalanceCache balanceCache;

    public ApproveAdjustmentCommand(AdjustmentRepository adjustmentRepository,
                                    PointsLedgerRepository ledgerRepository,
                                    RedisBalanceCache balanceCache) {
        this.adjustmentRepository = adjustmentRepository;
        this.ledgerRepository = ledgerRepository;
        this.balanceCache = balanceCache;
    }

    public record Request(UUID adjustmentId, UUID approverId, boolean approved) {}
    public record Response(UUID adjustmentId, String status, Integer newBalance) {}

    @Transactional
    public Response execute(Request request) {
        var adjustment = adjustmentRepository.findById(request.adjustmentId())
                .orElseThrow(() -> new IllegalArgumentException("ADJUSTMENT_NOT_FOUND"));

        if (adjustment.getStatus() != Adjustment.AdjustmentStatus.PENDING) {
            throw new IllegalStateException("ALREADY_REVIEWED");
        }
        if (adjustment.getStaffId().equals(request.approverId())) {
            throw new SecurityException("SELF_APPROVAL");
        }

        Integer newBalance = null;
        if (request.approved()) {
            adjustment.approve(request.approverId());
            int currentBalance = balanceCache.getBalance(adjustment.getCustomerId())
                    .orElseGet(() -> ledgerRepository.sumPointsByCustomerId(adjustment.getCustomerId()).orElse(0));
            int delta = adjustment.getAction() == Adjustment.AdjustmentAction.ADD
                    ? adjustment.getPoints() : -adjustment.getPoints();
            newBalance = currentBalance + delta;
            ledgerRepository.save(new PointsLedgerEntry(
                    adjustment.getCustomerId(), PointsLedgerEntry.TransactionType.ADJUST,
                    delta, newBalance, adjustment.getId().toString(), "system", null));
            balanceCache.setBalance(adjustment.getCustomerId(), newBalance);
        } else {
            adjustment.reject(request.approverId());
        }

        adjustmentRepository.save(adjustment);
        return new Response(adjustment.getId(), adjustment.getStatus().name().toLowerCase(), newBalance);
    }
}
