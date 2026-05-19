package co.uk.next.loyalty.admin.application.command;

import co.uk.next.loyalty.admin.application.dto.RatesConfigResponse;
import co.uk.next.loyalty.admin.domain.model.RatesConfig;
import co.uk.next.loyalty.admin.infrastructure.persistence.RatesConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UpdateRatesConfigCommand {

    private final RatesConfigRepository repository;

    public UpdateRatesConfigCommand(RatesConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RatesConfigResponse execute(Request request) {
        RatesConfig config = repository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("No rates config found"));

        if (request.accrualRate() != null) config.setAccrualRate(request.accrualRate());
        if (request.redemptionRate() != null) config.setRedemptionRate(request.redemptionRate());
        if (request.minimumRedemption() != null) config.setMinimumRedemption(request.minimumRedemption());
        if (request.maxDiscountPercentage() != null) config.setMaxDiscountPercentage(request.maxDiscountPercentage());
        if (request.expirationMonths() != null) config.setExpirationMonths(request.expirationMonths());
        config.setUpdatedAt(Instant.now());

        RatesConfig saved = repository.save(config);
        return new RatesConfigResponse(saved.getAccrualRate(), saved.getRedemptionRate(),
                saved.getMinimumRedemption(), saved.getMaxDiscountPercentage(),
                saved.getExpirationMonths(), saved.getUpdatedAt());
    }

    public record Request(Double accrualRate, Double redemptionRate, Integer minimumRedemption,
                           Double maxDiscountPercentage, Integer expirationMonths) {}
}
