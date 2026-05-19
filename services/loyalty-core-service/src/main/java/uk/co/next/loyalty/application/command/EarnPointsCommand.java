package uk.co.Dunelm.loyalty.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.Dunelm.loyalty.domain.model.Campaign;
import uk.co.Dunelm.loyalty.domain.model.PointsLedgerEntry;
import uk.co.Dunelm.loyalty.domain.port.CampaignRepository;
import uk.co.Dunelm.loyalty.domain.port.PointsLedgerRepository;
import uk.co.Dunelm.loyalty.infrastructure.cache.RedisBalanceCache;
import uk.co.Dunelm.loyalty.infrastructure.messaging.LoyaltyEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EarnPointsCommand {

    private final PointsLedgerRepository ledgerRepository;
    private final CampaignRepository campaignRepository;
    private final RedisBalanceCache balanceCache;
    private final LoyaltyEventPublisher eventPublisher;

    public EarnPointsCommand(PointsLedgerRepository ledgerRepository,
                             CampaignRepository campaignRepository,
                             RedisBalanceCache balanceCache,
                             LoyaltyEventPublisher eventPublisher) {
        this.ledgerRepository = ledgerRepository;
        this.campaignRepository = campaignRepository;
        this.balanceCache = balanceCache;
        this.eventPublisher = eventPublisher;
    }

    public record Request(UUID customerId, double transactionAmount, String referenceId, String channel) {}
    public record Response(UUID transactionId, int pointsEarned, int bonusPoints, int newBalance, String campaignApplied) {}

    @Transactional
    public Response execute(Request request) {
        // Idempotency check
        if (ledgerRepository.findByReferenceId(request.referenceId()).isPresent()) {
            throw new IllegalStateException("DUPLICATE_REFERENCE");
        }

        int basePoints = (int) Math.floor(request.transactionAmount());
        int bonusPoints = 0;
        String campaignApplied = null;

        // Check active campaigns
        List<Campaign> activeCampaigns = campaignRepository.findActiveAt(Instant.now());
        for (Campaign campaign : activeCampaigns) {
            if (!campaign.isWithinBudget(basePoints)) continue;
            if (campaign.getType() == Campaign.CampaignType.MULTIPLIER) {
                bonusPoints = (int) (basePoints * (campaign.getValue() - 1));
                campaignApplied = campaign.getName();
                campaign.addSpend(bonusPoints);
                campaignRepository.save(campaign);
                break;
            } else if (campaign.getType() == Campaign.CampaignType.FIXED_BONUS) {
                bonusPoints = (int) campaign.getValue();
                campaignApplied = campaign.getName();
                campaign.addSpend(bonusPoints);
                campaignRepository.save(campaign);
                break;
            }
        }

        int totalPoints = basePoints + bonusPoints;
        int currentBalance = balanceCache.getBalance(request.customerId())
                .orElseGet(() -> ledgerRepository.sumPointsByCustomerId(request.customerId()).orElse(0));
        int newBalance = currentBalance + totalPoints;

        var entry = new PointsLedgerEntry(
                request.customerId(), PointsLedgerEntry.TransactionType.EARN,
                totalPoints, newBalance, request.referenceId(), request.channel(), null);
        entry = ledgerRepository.save(entry);

        balanceCache.setBalance(request.customerId(), newBalance);
        eventPublisher.publishPointsEarned(request.customerId(), totalPoints, newBalance);

        return new Response(entry.getId(), basePoints, bonusPoints, newBalance, campaignApplied);
    }
}
