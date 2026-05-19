package co.uk.next.loyalty.notification.api.controller;

import co.uk.next.loyalty.notification.application.command.MarkNotificationReadCommand;
import co.uk.next.loyalty.notification.application.dto.NotificationResponse;
import co.uk.next.loyalty.notification.application.dto.PaginatedResponse;
import co.uk.next.loyalty.notification.application.query.GetNotificationsQuery;
import co.uk.next.loyalty.notification.application.query.GetUnreadCountQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final GetNotificationsQuery getNotificationsQuery;
    private final GetUnreadCountQuery getUnreadCountQuery;
    private final MarkNotificationReadCommand markNotificationReadCommand;

    public NotificationController(GetNotificationsQuery getNotificationsQuery,
                                  GetUnreadCountQuery getUnreadCountQuery,
                                  MarkNotificationReadCommand markNotificationReadCommand) {
        this.getNotificationsQuery = getNotificationsQuery;
        this.getUnreadCountQuery = getUnreadCountQuery;
        this.markNotificationReadCommand = markNotificationReadCommand;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<NotificationResponse>> listNotifications(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(getNotificationsQuery.execute(customerId, pageNumber, Math.min(pageSize, 100)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Boolean>> markRead(
            @PathVariable UUID id,
            @RequestHeader("X-Customer-Id") UUID customerId) {
        markNotificationReadCommand.execute(id, customerId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-Customer-Id") UUID customerId) {
        return ResponseEntity.ok(Map.of("unreadCount", getUnreadCountQuery.execute(customerId)));
    }
}
