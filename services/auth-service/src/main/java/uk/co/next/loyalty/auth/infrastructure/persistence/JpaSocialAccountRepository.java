package uk.co.Dunelm.loyalty.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.Dunelm.loyalty.auth.domain.model.SocialAccount;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSocialAccountRepository extends JpaRepository<SocialAccount, UUID> {
    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}
