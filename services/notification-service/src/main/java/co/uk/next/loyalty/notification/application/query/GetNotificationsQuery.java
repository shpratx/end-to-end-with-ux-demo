package co.uk.next.loyalty.notification.application.query;

import co.uk.next.loyalty.notification.application.dto.NotificationResponse;
import co.uk.next.loyalty.notification.application.dto.PaginatedResponse;
import co.uk.next.loyalty.notification.domain.port.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetNotificationsQuery {

    private final NotificationRepository notificationRepository;

    public GetNotificationsQuery(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public PaginatedResponse<NotificationResponse> execute(UUID customerId, int pageNumber, int pageSize) {
        var page = notificationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(
                customerId, PageRequest.of(pageNumber - 1, pageSize));
        var data = page.getContent().stream().map(NotificationResponse::from).toList();
        return new PaginatedResponse<>(data, pageNumber, pageSize, page.getTotalElements(), page.getTotalPages());
    }
}
