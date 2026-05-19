package co.uk.next.loyalty.admin.application.query;

import co.uk.next.loyalty.admin.application.command.CreateCampaignCommand;
import co.uk.next.loyalty.admin.application.dto.CampaignResponse;
import co.uk.next.loyalty.admin.infrastructure.persistence.CampaignRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCampaignsQuery {

    private final CampaignRepository repository;

    public GetCampaignsQuery(CampaignRepository repository) {
        this.repository = repository;
    }

    public List<CampaignResponse> execute(String status) {
        var campaigns = (status != null) ? repository.findByStatus(status) : repository.findAll();
        return campaigns.stream().map(CreateCampaignCommand::toCampaignResponse).toList();
    }
}
