package co.uk.next.loyalty.notification.api.controller;

import co.uk.next.loyalty.notification.application.command.MarkNotificationReadCommand;
import co.uk.next.loyalty.notification.application.dto.NotificationResponse;
import co.uk.next.loyalty.notification.application.dto.PaginatedResponse;
import co.uk.next.loyalty.notification.application.query.GetNotificationsQuery;
import co.uk.next.loyalty.notification.application.query.GetUnreadCountQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerContractTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private GetNotificationsQuery getNotificationsQuery;
    @MockBean private GetUnreadCountQuery getUnreadCountQuery;
    @MockBean private MarkNotificationReadCommand markNotificationReadCommand;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Test
    void listNotifications_returns200WithPaginatedData() throws Exception {
        var response = new PaginatedResponse<>(
                List.of(new NotificationResponse(UUID.randomUUID(), "Points Earned!", "You earned 76 points", "transactional", false, Instant.now())),
                1, 20, 1, 1);
        when(getNotificationsQuery.execute(eq(CUSTOMER_ID), eq(1), eq(20))).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications")
                        .header("X-Customer-Id", CUSTOMER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Points Earned!"))
                .andExpect(jsonPath("$.meta.pageNumber").value(1))
                .andExpect(jsonPath("$.meta.totalItems").value(1));
    }

    @Test
    void markRead_returns200WithSuccess() throws Exception {
        UUID notificationId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId)
                        .header("X-Customer-Id", CUSTOMER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUnreadCount_returns200WithCount() throws Exception {
        when(getUnreadCountQuery.execute(CUSTOMER_ID)).thenReturn(3L);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("X-Customer-Id", CUSTOMER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));
    }
}
