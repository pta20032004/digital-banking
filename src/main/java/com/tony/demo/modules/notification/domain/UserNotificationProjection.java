package com.tony.demo.modules.notification.domain;

import java.time.Instant;
import java.util.UUID;

public interface UserNotificationProjection {
    UUID getPublicId();
    String getTitle();
    String getContent();
    String getType();
    Boolean getIsRead();
    Boolean getIsBroadcast();
    String getActionUrl();
    Instant getCreatedAt();
    String getParams();
    String getTemplateCode();
}
