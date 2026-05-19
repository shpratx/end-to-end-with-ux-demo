package uk.co.next.loyalty.application.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.co.next.loyalty.domain.model.PointsLedgerEntry;
import uk.co.next.loyalty.domain.port.PointsLedgerRepository;

import java.util.UUID;

@Service
public class GetTransactionsQuery {

    private final PointsLedgerRepository ledgerRepository;

    public GetTransactionsQuery(PointsLedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public Page<PointsLedgerEntry> execute(UUID customerId, String type, int page, int size) {
        var pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (type != null) {
            return ledgerRepository.findByCustomerIdAndType(customerId,
                    PointsLedgerEntry.TransactionType.valueOf(type.toUpperCase()), pageable);
        }
        return ledgerRepository.findByCustomerId(customerId, pageable);
    }
}
