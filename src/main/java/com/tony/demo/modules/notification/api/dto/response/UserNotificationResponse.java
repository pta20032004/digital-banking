package com.tony.demo.modules.notification.api.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record UserNotificationResponse(
    long unreadCount,
    List<UserNotificationDTO> data
) {}
