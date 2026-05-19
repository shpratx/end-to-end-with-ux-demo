package co.uk.next.loyalty.notification.domain.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false, unique = true, length = 100)
    private String eventType;

    @Column(name = "title_template", nullable = false)
    private String titleTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(nullable = false)
    private String channels;

    @Column(nullable = false)
    private boolean active = true;

    protected NotificationTemplate() {}

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getTitleTemplate() { return titleTemplate; }
    public String getBodyTemplate() { return bodyTemplate; }
    public String getChannels() { return channels; }
    public boolean isActive() { return active; }

    public String renderTitle(java.util.Map<String, String> variables) {
        return render(titleTemplate, variables);
    }

    public String renderBody(java.util.Map<String, String> variables) {
        return render(bodyTemplate, variables);
    }

    private String render(String template, java.util.Map<String, String> variables) {
        String result = template;
        for (var entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
