package co.uk.next.loyalty.admin.application.query;

import co.uk.next.loyalty.admin.application.dto.DashboardResponse;
import co.uk.next.loyalty.admin.infrastructure.persistence.CampaignRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAdminDashboardQuery {

    private final CampaignRepository campaignRepository;

    public GetAdminDashboardQuery(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public DashboardResponse execute() {
        int activeCampaigns = campaignRepository.countByStatus("active");
        return new DashboardResponse(0, 0, 0.0, 0L, activeCampaigns, 0);
    }
}
