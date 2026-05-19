package uk.co.Dunelm.loyalty.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.co.Dunelm.loyalty.domain.model.Campaign;
import uk.co.Dunelm.loyalty.domain.port.CampaignRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaCampaignRepository extends JpaRepository<Campaign, UUID>, CampaignRepository {

    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= :now AND c.endDate >= :now")
    List<Campaign> findActiveAt(Instant now);
}
