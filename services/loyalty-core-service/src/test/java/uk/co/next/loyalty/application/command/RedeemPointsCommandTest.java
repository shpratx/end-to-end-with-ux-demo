package uk.co.Dunelm.loyalty.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.Dunelm.loyalty.domain.port.PointsLedgerRepository;
import uk.co.Dunelm.loyalty.infrastructure.cache.RedisBalanceCache;
import uk.co.Dunelm.loyalty.infrastructure.messaging.LoyaltyEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedeemPointsCommandTest {

    @Mock private PointsLedgerRepository ledgerRepository;
    @Mock private RedisBalanceCache balanceCache;
    @Mock private LoyaltyEventPublisher eventPublisher;

    private RedeemPointsCommand command;

    @BeforeEach
    void setUp() {
        command = new RedeemPointsCommand(ledgerRepository, balanceCache, eventPublisher);
        // Set fields via reflection for test
        try {
            var minField = RedeemPointsCommand.class.getDeclaredField("minThreshold");
            minField.setAccessible(true);
            minField.setInt(command, 100);
            var maxField = RedeemPointsCommand.class.getDeclaredField("maxDiscountPercent");
            maxField.setAccessible(true);
            maxField.setInt(command, 50);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void successfulRedeem_deductsPointsAndReturnsDiscount() {
        var customerId = UUID.randomUUID();
        when(ledgerRepository.findByReferenceId(any())).thenReturn(Optional.empty());
        when(balanceCache.getBalance(customerId)).thenReturn(Optional.of(2000));
        when(ledgerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = command.execute(new RedeemPointsCommand.Request(customerId, 500, "ORD-R1", 120.0, "online"));

        assertTrue(result.success());
        assertEquals(500, result.pointsRedeemed());
        assertEquals(25.0, result.discountApplied());
        assertEquals(1500, result.remainingBalance());
    }

    @Test
    void insufficientBalance_throwsException() {
        var customerId = UUID.randomUUID();
        when(ledgerRepository.findByReferenceId(any())).thenReturn(Optional.empty());
        when(balanceCache.getBalance(customerId)).thenReturn(Optional.of(50));

        assertThrows(IllegalArgumentException.class, () ->
                command.execute(new RedeemPointsCommand.Request(customerId, 500, "ORD-R2", 100.0, "online")));
    }

    @Test
    void belowMinimumThreshold_throwsException() {
        var customerId = UUID.randomUUID();
        when(ledgerRepository.findByReferenceId(any())).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class, () ->
                command.execute(new RedeemPointsCommand.Request(customerId, 50, "ORD-R3", 100.0, "online")));
        assertEquals("BELOW_MINIMUM_REDEMPTION", ex.getMessage());
    }

    @Test
    void exceedsMaxDiscount_throwsException() {
        var customerId = UUID.randomUUID();
        when(ledgerRepository.findByReferenceId(any())).thenReturn(Optional.empty());
        when(balanceCache.getBalance(customerId)).thenReturn(Optional.of(5000));

        // 5000 points = £250 discount, but 50% of £100 order = £50 max
        var ex = assertThrows(IllegalArgumentException.class, () ->
                command.execute(new RedeemPointsCommand.Request(customerId, 5000, "ORD-R4", 100.0, "online")));
        assertEquals("EXCEEDS_MAX_DISCOUNT", ex.getMessage());
    }
}
