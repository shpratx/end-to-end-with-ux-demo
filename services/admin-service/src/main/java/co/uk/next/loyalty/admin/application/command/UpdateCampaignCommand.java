package co.uk.next.loyalty.admin.application.command;

import co.uk.next.loyalty.admin.application.dto.CampaignResponse;
import co.uk.next.loyalty.admin.domain.model.Campaign;
import co.uk.next.loyalty.admin.infrastructure.persistence.CampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateCampaignCommand {

    private final CampaignRepository repository;

    public UpdateCampaignCommand(CampaignRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CampaignResponse execute(UUID id, Request request) {
        Campaign campaign = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        if (request.name() != null) campaign.setName(request.name());
        if (request.status() != null) campaign.setStatus(request.status());
        if (request.endDate() != null) campaign.setEndDate(request.endDate());
        if (request.maxBudget() != null) campaign.setMaxBudget(request.maxBudget());
        campaign.setUpdatedAt(Instant.now());

        Campaign saved = repository.save(campaign);
        return CreateCampaignCommand.toCampaignResponse(saved);
    }

    public record Request(String name, String status, Instant endDate, Integer maxBudget) {}
}
