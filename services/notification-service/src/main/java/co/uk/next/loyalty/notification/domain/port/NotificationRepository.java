package co.uk.next.loyalty.notification.domain.port;

import co.uk.next.loyalty.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customerId = :customerId AND n.readAt IS NULL AND n.deleted = false")
    long countUnreadByCustomerId(UUID customerId);
}
