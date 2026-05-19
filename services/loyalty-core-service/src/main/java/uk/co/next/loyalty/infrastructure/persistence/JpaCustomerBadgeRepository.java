package uk.co.next.loyalty.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.next.loyalty.domain.model.CustomerBadge;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaCustomerBadgeRepository extends JpaRepository<CustomerBadge, UUID> {
    List<CustomerBadge> findByCustomerId(UUID customerId);
}
