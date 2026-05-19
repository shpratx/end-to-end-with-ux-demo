package co.uk.Dunelm.loyalty.notification.domain.event;

public interface DomainEvent {
    String eventType();
    java.time.Instant occurredAt();
}
