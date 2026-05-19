package com.tony.demo.modules.notification.api.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserNotificationDTO(
    UUID publicId,
    String title,
    String content,
    String type,
    Boolean isRead,
    Boolean isBroadcast,
    String actionUrl,
    Instant createdAt
) {}
