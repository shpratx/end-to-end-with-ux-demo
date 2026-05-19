package uk.co.next.loyalty.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.next.loyalty.auth.domain.model.Consent;

import java.util.UUID;

@Repository
public interface JpaConsentRepository extends JpaRepository<Consent, UUID> {
}
