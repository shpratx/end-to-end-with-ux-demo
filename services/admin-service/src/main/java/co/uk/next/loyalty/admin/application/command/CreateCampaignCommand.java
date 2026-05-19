package co.uk.Dunelm.loyalty.admin.application.command;

import co.uk.Dunelm.loyalty.admin.application.dto.CampaignResponse;
import co.uk.Dunelm.loyalty.admin.domain.model.Campaign;
import co.uk.Dunelm.loyalty.admin.infrastructure.persistence.CampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateCampaignCommand {

    private final CampaignRepository repository;

    public CreateCampaignCommand(CampaignRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CampaignResponse execute(Request request) {
        Campaign campaign = new Campaign();
        campaign.setName(request.name());
        campaign.setType(request.type());
        campaign.setValue(request.value());
        campaign.setStartDate(request.startDate());
        campaign.setEndDate(request.endDate());
        campaign.setEligibility(request.eligibility());
        campaign.setMinimumTier(request.minimumTier());
        campaign.setMaxBudget(request.maxBudget());
        campaign.setBudgetUsed(0);
        campaign.setStatus("draft");
        campaign.setCreatedAt(Instant.now());
        campaign.setUpdatedAt(Instant.now());

        Campaign saved = repository.save(campaign);
        return toCampaignResponse(saved);
    }

    public static CampaignResponse toCampaignResponse(Campaign c) {
        return new CampaignResponse(c.getId(), c.getName(), c.getType(), c.getValue(),
                c.getStartDate(), c.getEndDate(), c.getEligibility(), c.getMinimumTier(),
                c.getMaxBudget(), c.getBudgetUsed(), c.getStatus(), c.getCreatedAt());
    }

    public record Request(String name, String type, double value, Instant startDate,
                           Instant endDate, String eligibility, String minimumTier, Integer maxBudget) {}
}
