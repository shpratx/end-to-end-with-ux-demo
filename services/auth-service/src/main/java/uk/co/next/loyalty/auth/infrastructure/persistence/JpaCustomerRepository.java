package uk.co.next.loyalty.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.next.loyalty.auth.domain.model.Customer;
import uk.co.next.loyalty.auth.domain.port.CustomerRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, UUID>, CustomerRepository {
    Optional<Customer> findByEmailAndDeletedFalse(String email);
    boolean existsByEmailAndDeletedFalse(String email);

    @Override
    default Optional<Customer> findByEmail(String email) {
        return findByEmailAndDeletedFalse(email);
    }

    @Override
    default boolean existsByEmail(String email) {
        return existsByEmailAndDeletedFalse(email);
    }
}
