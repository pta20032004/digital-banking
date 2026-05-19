package com.tony.demo.modules.notification.api.dto;

import java.time.Instant;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendBroadcastNotificationRequest {

    // For manual message without template
    private String title;
    private String content;

    // For template
    private String templateCode;
    private Map<String, Object> params;

    private String type; // e.g., SYSTEM, PROMOTION
    private String actionUrl;

    @NotNull(message = "startTime is required for broadcast")
    private Instant startTime;

    private Instant endTime;
}
