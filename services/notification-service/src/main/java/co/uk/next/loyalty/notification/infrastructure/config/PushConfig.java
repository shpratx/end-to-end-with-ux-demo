package co.uk.next.loyalty.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification.push")
public class PushConfig {
    private String fcmServerKey;
    private String apnsKeyId;
    private String apnsTeamId;
    private String apnsBundleId;

    public String getFcmServerKey() { return fcmServerKey; }
    public void setFcmServerKey(String fcmServerKey) { this.fcmServerKey = fcmServerKey; }
    public String getApnsKeyId() { return apnsKeyId; }
    public void setApnsKeyId(String apnsKeyId) { this.apnsKeyId = apnsKeyId; }
    public String getApnsTeamId() { return apnsTeamId; }
    public void setApnsTeamId(String apnsTeamId) { this.apnsTeamId = apnsTeamId; }
    public String getApnsBundleId() { return apnsBundleId; }
    public void setApnsBundleId(String apnsBundleId) { this.apnsBundleId = apnsBundleId; }
}
