package co.uk.Dunelm.loyalty.admin.infrastructure.persistence;

import co.uk.Dunelm.loyalty.admin.domain.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    List<Campaign> findByStatus(String status);
    int countByStatus(String status);
}
