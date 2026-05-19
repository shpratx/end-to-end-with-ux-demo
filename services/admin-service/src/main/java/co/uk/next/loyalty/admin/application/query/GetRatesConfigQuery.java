package co.uk.Dunelm.loyalty.admin.application.query;

import co.uk.Dunelm.loyalty.admin.application.dto.RatesConfigResponse;
import co.uk.Dunelm.loyalty.admin.domain.model.RatesConfig;
import co.uk.Dunelm.loyalty.admin.infrastructure.persistence.RatesConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class GetRatesConfigQuery {

    private final RatesConfigRepository repository;

    public GetRatesConfigQuery(RatesConfigRepository repository) {
        this.repository = repository;
    }

    public RatesConfigResponse execute() {
        RatesConfig config = repository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("No rates config found"));
        return new RatesConfigResponse(config.getAccrualRate(), config.getRedemptionRate(),
                config.getMinimumRedemption(), config.getMaxDiscountPercentage(),
                config.getExpirationMonths(), config.getUpdatedAt());
    }
}
