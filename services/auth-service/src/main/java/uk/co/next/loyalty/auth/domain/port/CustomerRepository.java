package uk.co.Dunelm.loyalty.auth.domain.port;

import uk.co.Dunelm.loyalty.auth.domain.model.Customer;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
}
