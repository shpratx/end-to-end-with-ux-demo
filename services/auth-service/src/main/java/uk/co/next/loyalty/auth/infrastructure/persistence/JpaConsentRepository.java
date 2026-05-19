package uk.co.Dunelm.loyalty.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.Dunelm.loyalty.auth.domain.model.Consent;

import java.util.UUID;

@Repository
public interface JpaConsentRepository extends JpaRepository<Consent, UUID> {
}
