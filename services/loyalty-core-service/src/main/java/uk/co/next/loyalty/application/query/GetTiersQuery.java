package uk.co.next.loyalty.application.query;

import org.springframework.stereotype.Service;
import uk.co.next.loyalty.domain.model.Tier;
import uk.co.next.loyalty.domain.port.TierRepository;

import java.util.List;

@Service
public class GetTiersQuery {

    private final TierRepository tierRepository;

    public GetTiersQuery(TierRepository tierRepository) {
        this.tierRepository = tierRepository;
    }

    public List<Tier> execute() {
        return tierRepository.findAllByOrderBySortOrder();
    }
}
