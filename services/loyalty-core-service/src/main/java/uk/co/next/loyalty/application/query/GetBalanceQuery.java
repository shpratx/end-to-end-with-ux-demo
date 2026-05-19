package uk.co.next.loyalty.application.query;

import org.springframework.stereotype.Service;
import uk.co.next.loyalty.domain.port.PointsLedgerRepository;
import uk.co.next.loyalty.infrastructure.cache.RedisBalanceCache;

import java.time.Instant;
import java.util.UUID;

@Service
public class GetBalanceQuery {

    private final RedisBalanceCache balanceCache;
    private final PointsLedgerRepository ledgerRepository;

    public GetBalanceQuery(RedisBalanceCache balanceCache, PointsLedgerRepository ledgerRepository) {
        this.balanceCache = balanceCache;
        this.ledgerRepository = ledgerRepository;
    }

    public record Response(int availablePoints, double monetaryEquivalent, int pendingPoints, double tierMultiplier, Instant lastUpdated) {}

    public Response execute(UUID customerId) {
        int balance = balanceCache.getBalance(customerId)
                .orElseGet(() -> {
                    int computed = ledgerRepository.sumPointsByCustomerId(customerId).orElse(0);
                    balanceCache.setBalance(customerId, computed);
                    return computed;
                });
        double monetary = balance / 100.0 * 5.0;
        return new Response(balance, monetary, 0, 1.0, Instant.now());
    }
}
