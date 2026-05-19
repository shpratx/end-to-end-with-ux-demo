package co.uk.Dunelm.loyalty.admin.infrastructure.persistence;

import co.uk.Dunelm.loyalty.admin.domain.model.RatesConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RatesConfigRepository extends JpaRepository<RatesConfig, UUID> {
    Optional<RatesConfig> findFirstByOrderByUpdatedAtDesc();
}
