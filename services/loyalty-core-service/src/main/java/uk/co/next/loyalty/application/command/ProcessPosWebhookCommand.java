package uk.co.next.loyalty.application.command;

import org.springframework.stereotype.Service;
import uk.co.next.loyalty.domain.port.PointsLedgerRepository;

import java.util.UUID;

@Service
public class ProcessPosWebhookCommand {

    private final EarnPointsCommand earnPointsCommand;
    private final PointsLedgerRepository ledgerRepository;

    public ProcessPosWebhookCommand(EarnPointsCommand earnPointsCommand,
                                    PointsLedgerRepository ledgerRepository) {
        this.earnPointsCommand = earnPointsCommand;
        this.ledgerRepository = ledgerRepository;
    }

    public record Request(String eventType, String transactionId, UUID customerId, double totalAmount) {}
    public record Response(boolean accepted, UUID correlationId) {}

    public Response execute(Request request) {
        // Dedup by transactionId
        if (ledgerRepository.findByReferenceId(request.transactionId()).isPresent()) {
            return new Response(true, UUID.randomUUID());
        }

        if ("transaction.completed".equals(request.eventType())) {
            earnPointsCommand.execute(new EarnPointsCommand.Request(
                    request.customerId(), request.totalAmount(), request.transactionId(), "in_store"));
        }

        return new Response(true, UUID.randomUUID());
    }
}
