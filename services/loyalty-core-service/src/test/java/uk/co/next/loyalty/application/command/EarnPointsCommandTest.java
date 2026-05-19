package uk.co.next.loyalty.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.next.loyalty.domain.model.Campaign;
import uk.co.next.loyalty.domain.model.PointsLedgerEntry;
import uk.co.next.loyalty.domain.port.CampaignRepository;
import uk.co.next.loyalty.domain.port.PointsLedgerRepository;
import uk.co.next.loyalty.infrastructure.cache.RedisBalanceCache;
import uk.co.next.loyalty.infrastructure.messaging.LoyaltyEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EarnPointsCommandTest {

    @Mock private PointsLedgerRepository ledgerRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private RedisBalanceCache balanceCache;
    @Mock private LoyaltyEventPublisher eventPublisher;

    private EarnPointsCommand command;

    @BeforeEach
    void setUp() {
        command = new EarnPointsCommand(ledgerRepository, campaignRepository, balanceCache, eventPublisher);
    }

    @Test
    void basicEarn_creditsPointsBasedOnAmount() {
        var customerId = UUID.randomUUID();
        when(ledgerRepository.findByReferenceId(any())).thenReturn(Optional.empty());
        when(campaignRepository.findActiveAt(any())).thenReturn(List.of());
        when(balanceCache.getBalance(customerId)).thenReturn(Optional.of(1000));
        when(ledgerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = command.execute(new EarnPointsCommand.Request(customerId, 75.50, "ORD-001", "online"));

        assertEquals(75, result.pointsEarned());
        assertEquals(0, result.bonusPoints());
        assertEquals(1075, result.newBalance());
        assertNull(result.campaignApplied());
        verify(eventPublisher).publishPointsEarned(customerId, 75, 1075);
    }

    @Test
    void earnWithCampaignMultiplier_appliesBonusPoints() {
        var customerId = UUID.randomUUID();
        var campaign = mock(Campaign.class);
        when(campaign.getType()).thenReturn(Campaign.CampaignType.MULTIPLIER);
        when(campaign.getValue()).thenReturn(2.0);
        when(campaign.isWithinBudget(anyInt())).thenReturn(true);
        when(campaign.getName()).thenReturn("Double Points");

        when(ledgerRepository.findByReferenceId(any())).thenReturn(Optional.empty());
        when(campaignRepository.findActiveAt(any())).thenReturn(List.of(campaign));
        when(balanceCache.getBalance(customerId)).thenReturn(Optional.of(500));
        when(ledgerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = command.execute(new EarnPointsCommand.Request(customerId, 100.0, "ORD-002", "in_store"));

        assertEquals(100, result.pointsEarned());
        assertEquals(100, result.bonusPoints());
        assertEquals(700, result.newBalance());
        assertEquals("Double Points", result.campaignApplied());
    }

    @Test
    void duplicateReference_throwsException() {
        when(ledgerRepository.findByReferenceId("ORD-DUP")).thenReturn(Optional.of(mock(PointsLedgerEntry.class)));

        assertThrows(IllegalStateException.class, () ->
                command.execute(new EarnPointsCommand.Request(UUID.randomUUID(), 50.0, "ORD-DUP", "online")));
    }

    @Test
    void customerNotFound_fallsBackToLedgerSum() {
        var customerId = UUID.randomUUID();
        when(ledgerRepository.findByReferenceId(any())).thenReturn(Optional.empty());
        when(campaignRepository.findActiveAt(any())).thenReturn(List.of());
        when(balanceCache.getBalance(customerId)).thenReturn(Optional.empty());
        when(ledgerRepository.sumPointsByCustomerId(customerId)).thenReturn(Optional.of(200));
        when(ledgerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = command.execute(new EarnPointsCommand.Request(customerId, 30.0, "ORD-003", "app"));

        assertEquals(230, result.newBalance());
    }
}
