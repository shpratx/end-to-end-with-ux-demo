package co.uk.next.loyalty.notification.domain.event;

public interface DomainEvent {
    String eventType();
    java.time.Instant occurredAt();
}
