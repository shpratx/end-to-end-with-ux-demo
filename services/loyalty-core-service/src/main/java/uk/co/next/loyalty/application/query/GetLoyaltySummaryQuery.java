package uk.co.Dunelm.loyalty.application.query;

import org.springframework.stereotype.Service;
import uk.co.Dunelm.loyalty.domain.model.PointsLedgerEntry;
import uk.co.Dunelm.loyalty.domain.port.PointsLedgerRepository;

import java.util.List;
import java.util.UUID;

@Service
public class GetLoyaltySummaryQuery {

    private final PointsLedgerRepository ledgerRepository;
    private final GetBalanceQuery getBalanceQuery;

    public GetLoyaltySummaryQuery(PointsLedgerRepository ledgerRepository, GetBalanceQuery getBalanceQuery) {
        this.ledgerRepository = ledgerRepository;
        this.getBalanceQuery = getBalanceQuery;
    }

    public record Response(UUID customerId, int pointsBalance, double monetaryEquivalent,
                           List<PointsLedgerEntry> recentTransactions) {}

    public Response execute(UUID customerId) {
        var balance = getBalanceQuery.execute(customerId);
        var recent = ledgerRepository.findTop5ByCustomerIdOrderByCreatedAtDesc(customerId);
        return new Response(customerId, balance.availablePoints(), balance.monetaryEquivalent(), recent);
    }
}
