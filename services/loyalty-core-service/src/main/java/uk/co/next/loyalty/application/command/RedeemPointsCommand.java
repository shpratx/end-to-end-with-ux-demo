package uk.co.next.loyalty.application.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.next.loyalty.domain.model.PointsLedgerEntry;
import uk.co.next.loyalty.domain.port.PointsLedgerRepository;
import uk.co.next.loyalty.infrastructure.cache.RedisBalanceCache;
import uk.co.next.loyalty.infrastructure.messaging.LoyaltyEventPublisher;

import java.util.UUID;

@Service
public class RedeemPointsCommand {

    private final PointsLedgerRepository ledgerRepository;
    private final RedisBalanceCache balanceCache;
    private final LoyaltyEventPublisher eventPublisher;

    @Value("${loyalty.redemption.min-threshold:100}")
    private int minThreshold;

    @Value("${loyalty.redemption.max-discount-percent:50}")
    private int maxDiscountPercent;

    public RedeemPointsCommand(PointsLedgerRepository ledgerRepository,
                               RedisBalanceCache balanceCache,
                               LoyaltyEventPublisher eventPublisher) {
        this.ledgerRepository = ledgerRepository;
        this.balanceCache = balanceCache;
        this.eventPublisher = eventPublisher;
    }

    public record Request(UUID customerId, int pointsToRedeem, String orderId, double orderTotal, String channel) {}
    public record Response(boolean success, UUID transactionId, int pointsRedeemed, double discountApplied, int remainingBalance) {}

    @Transactional
    public Response execute(Request request) {
        // Idempotency
        if (ledgerRepository.findByReferenceId(request.orderId()).isPresent()) {
            throw new IllegalStateException("DUPLICATE_REFERENCE");
        }

        if (request.pointsToRedeem() < minThreshold) {
            throw new IllegalArgumentException("BELOW_MINIMUM_REDEMPTION");
        }

        int currentBalance = balanceCache.getBalance(request.customerId())
                .orElseGet(() -> ledgerRepository.sumPointsByCustomerId(request.customerId()).orElse(0));

        if (currentBalance < request.pointsToRedeem()) {
            throw new IllegalArgumentException("INSUFFICIENT_BALANCE");
        }

        double discount = request.pointsToRedeem() / 100.0 * 5.0;
        double maxDiscount = request.orderTotal() * maxDiscountPercent / 100.0;
        if (discount > maxDiscount) {
            throw new IllegalArgumentException("EXCEEDS_MAX_DISCOUNT");
        }

        int newBalance = currentBalance - request.pointsToRedeem();
        var entry = new PointsLedgerEntry(
                request.customerId(), PointsLedgerEntry.TransactionType.REDEEM,
                -request.pointsToRedeem(), newBalance, request.orderId(), request.channel(), null);
        entry = ledgerRepository.save(entry);

        balanceCache.setBalance(request.customerId(), newBalance);
        eventPublisher.publishPointsRedeemed(request.customerId(), request.pointsToRedeem(), newBalance);

        return new Response(true, entry.getId(), request.pointsToRedeem(), discount, newBalance);
    }
}
