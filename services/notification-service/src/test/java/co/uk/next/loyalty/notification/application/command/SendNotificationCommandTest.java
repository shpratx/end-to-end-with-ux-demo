package co.uk.Dunelm.loyalty.notification.application.command;

import co.uk.Dunelm.loyalty.notification.domain.model.Notification;
import co.uk.Dunelm.loyalty.notification.domain.model.NotificationTemplate;
import co.uk.Dunelm.loyalty.notification.domain.model.PushToken;
import co.uk.Dunelm.loyalty.notification.domain.port.NotificationRepository;
import co.uk.Dunelm.loyalty.notification.domain.port.PushTokenRepository;
import co.uk.Dunelm.loyalty.notification.domain.port.TemplateRepository;
import co.uk.Dunelm.loyalty.notification.infrastructure.client.ApnsClient;
import co.uk.Dunelm.loyalty.notification.infrastructure.client.EmailClient;
import co.uk.Dunelm.loyalty.notification.infrastructure.client.FcmClient;
import co.uk.Dunelm.loyalty.notification.infrastructure.messaging.publisher.NotificationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationCommandTest {

    @Mock private TemplateRepository templateRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private PushTokenRepository pushTokenRepository;
    @Mock private FcmClient fcmClient;
    @Mock private ApnsClient apnsClient;
    @Mock private EmailClient emailClient;
    @Mock private NotificationEventPublisher eventPublisher;

    private SendNotificationCommand command;

    @BeforeEach
    void setUp() {
        command = new SendNotificationCommand(templateRepository, notificationRepository,
                pushTokenRepository, fcmClient, apnsClient, emailClient, eventPublisher);
        command.setBackoffMs(new long[]{0L, 0L, 0L});
    }

    @Test
    void execute_withValidTemplate_sendsInAppNotification() {
        UUID customerId = UUID.randomUUID();
        NotificationTemplate template = createTemplate("customer.registered",
                "Welcome!", "Hi {name}!", "in_app");

        when(templateRepository.findByEventTypeAndActiveTrue("customer.registered"))
                .thenReturn(Optional.of(template));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(eq(customerId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Notification(customerId, "Welcome!", "Hi John!", "transactional", "in_app"))));

        command.execute(new SendNotificationCommand.Input(
                customerId, "customer.registered", Map.of("name", "John"), List.of("in_app")));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Welcome!");
        assertThat(captor.getValue().getBody()).isEqualTo("Hi John!");
        assertThat(captor.getValue().getChannel()).isEqualTo("in_app");
    }

    @Test
    void execute_templateNotFound_throwsException() {
        when(templateRepository.findByEventTypeAndActiveTrue("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> command.execute(new SendNotificationCommand.Input(
                UUID.randomUUID(), "unknown", Map.of(), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template not found");
    }

    @Test
    void execute_pushFails_fallsBackToEmail() {
        UUID customerId = UUID.randomUUID();
        NotificationTemplate template = createTemplate("points.earned",
                "Points!", "You earned {points} points!", "push");
        PushToken token = new PushToken(customerId, "android", "fcm-token-123");

        when(templateRepository.findByEventTypeAndActiveTrue("points.earned"))
                .thenReturn(Optional.of(template));
        when(pushTokenRepository.findByCustomerIdAndActiveTrue(customerId))
                .thenReturn(List.of(token));
        doThrow(new RuntimeException("FCM down")).when(fcmClient).send(any(), any(), any());
        doNothing().when(emailClient).send(eq(customerId), any(), any());
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(eq(customerId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Notification(customerId, "Points!", "You earned 50 points!", "transactional", "email"))));

        command.execute(new SendNotificationCommand.Input(
                customerId, "points.earned", Map.of("points", "50"), List.of("push")));

        verify(emailClient, atLeastOnce()).send(eq(customerId), eq("Points!"), eq("You earned 50 points!"));
    }

    @Test
    void execute_allDeliveryFails_publishesFailedEvent() {
        UUID customerId = UUID.randomUUID();
        NotificationTemplate template = createTemplate("points.earned",
                "Points!", "You earned {points}!", "push");
        PushToken token = new PushToken(customerId, "android", "fcm-token-123");

        when(templateRepository.findByEventTypeAndActiveTrue("points.earned"))
                .thenReturn(Optional.of(template));
        when(pushTokenRepository.findByCustomerIdAndActiveTrue(customerId))
                .thenReturn(List.of(token));
        doThrow(new RuntimeException("FCM down")).when(fcmClient).send(any(), any(), any());
        doThrow(new RuntimeException("Email down")).when(emailClient).send(any(), any(), any());
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> {
                    Notification n = inv.getArgument(0);
                    setField(n, "id", UUID.randomUUID());
                    return n;
                });

        command.execute(new SendNotificationCommand.Input(
                customerId, "points.earned", Map.of("points", "50"), List.of("push")));

        verify(eventPublisher).publishFailed(any(UUID.class), eq("push"), any(), eq(3));
    }

    @Test
    void execute_variableSubstitution_rendersCorrectly() {
        UUID customerId = UUID.randomUUID();
        NotificationTemplate template = createTemplate("tier.upgraded",
                "Tier Upgrade!", "Welcome to {tier} tier with {balance} points!", "in_app");

        when(templateRepository.findByEventTypeAndActiveTrue("tier.upgraded"))
                .thenReturn(Optional.of(template));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(eq(customerId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Notification(customerId, "Tier Upgrade!", "Welcome to Gold tier with 5000 points!", "transactional", "in_app"))));

        command.execute(new SendNotificationCommand.Input(
                customerId, "tier.upgraded", Map.of("tier", "Gold", "balance", "5000"), null));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getBody()).isEqualTo("Welcome to Gold tier with 5000 points!");
    }

    private NotificationTemplate createTemplate(String eventType, String title, String body, String channels) {
        try {
            NotificationTemplate t = new NotificationTemplate();
            setField(t, "id", UUID.randomUUID());
            setField(t, "eventType", eventType);
            setField(t, "titleTemplate", title);
            setField(t, "bodyTemplate", body);
            setField(t, "channels", channels);
            setField(t, "active", true);
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
