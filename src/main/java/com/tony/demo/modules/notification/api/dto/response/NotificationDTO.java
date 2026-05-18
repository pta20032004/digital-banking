package com.tony.demo.modules.notification.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationDTO(
    UUID publicId,
    String title,
    String content,
    String type,
    Boolean isRead,
    Instant readAt,
    String actionUrl,
    Instant createdAt
) {}
