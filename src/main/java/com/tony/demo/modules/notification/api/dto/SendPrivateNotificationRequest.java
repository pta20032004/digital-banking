package com.tony.demo.modules.notification.api.dto;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendPrivateNotificationRequest {
    
    @NotNull(message = "userPublicId must not be null")
    private UUID userPublicId;
    
    // For manual message without template
    private String title;
    private String content;

    // For template
    private String templateCode;
    private Map<String, Object> params;

    private String type; // e.g., SECURITY, SYSTEM, PROMOTION
    private String actionUrl;
}
