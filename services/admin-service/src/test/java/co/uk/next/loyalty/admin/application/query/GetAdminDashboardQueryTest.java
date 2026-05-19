package co.uk.next.loyalty.admin.application.query;

import co.uk.next.loyalty.admin.application.dto.DashboardResponse;
import co.uk.next.loyalty.admin.domain.model.Campaign;
import co.uk.next.loyalty.admin.infrastructure.persistence.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GetAdminDashboardQueryTest {

    @Autowired
    private GetAdminDashboardQuery query;

    @Autowired
    private CampaignRepository campaignRepository;

    @BeforeEach
    void setUp() {
        campaignRepository.deleteAll();
    }

    @Test
    void returnsAggregatedMetrics() {
        Campaign c = new Campaign();
        c.setName("Summer Double Points");
        c.setType("multiplier");
        c.setValue(2.0);
        c.setStartDate(Instant.now());
        c.setEndDate(Instant.now().plusSeconds(86400));
        c.setEligibility("all_customers");
        c.setBudgetUsed(0);
        c.setStatus("active");
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());
        campaignRepository.save(c);

        DashboardResponse response = query.execute();

        assertThat(response.activeCampaigns()).isEqualTo(1);
        assertThat(response).isNotNull();
    }

    @Test
    void emptyStateReturnsZeros() {
        DashboardResponse response = query.execute();

        assertThat(response.activeMembers()).isZero();
        assertThat(response.signUpsThisMonth()).isZero();
        assertThat(response.redemptionRate()).isZero();
        assertThat(response.totalPointsLiability()).isZero();
        assertThat(response.activeCampaigns()).isZero();
        assertThat(response.transactionsToday()).isZero();
    }
}
