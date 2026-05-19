package com.tony.demo.modules.notification.application;

import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tony.demo.modules.notification.api.dto.SendBroadcastNotificationRequest;
import com.tony.demo.modules.notification.api.dto.SendPrivateNotificationRequest;
import com.tony.demo.modules.notification.domain.BroadcastNotification;
import com.tony.demo.modules.notification.domain.BroadcastNotificationRepository;
import com.tony.demo.modules.notification.domain.Notification;
import com.tony.demo.modules.notification.domain.NotificationRepository;
import com.tony.demo.modules.notification.domain.NotificationTemplate;
import com.tony.demo.modules.notification.domain.NotificationTemplateRepository;
import com.tony.demo.modules.notification.domain.NotificationType;
import com.tony.demo.modules.system.domain.AuditLog;
import com.tony.demo.modules.system.domain.AuditLogRepository;
import com.tony.demo.modules.user.domain.Staff;
import com.tony.demo.modules.user.domain.StaffRepository;
import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final BroadcastNotificationRepository broadcastNotificationRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final AuditLogRepository auditLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Transactional
    public void sendPrivateNotification(SendPrivateNotificationRequest request) {
        User user = userRepository.findByPublicId(request.getUserPublicId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserPublicId()));

        NotificationTemplate template = null;
        if (request.getTemplateCode() != null) {
            template = notificationTemplateRepository.findById(request.getTemplateCode())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found: " + request.getTemplateCode()));
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .template(template)
                .params(request.getParams())
                .type(request.getType() != null ? NotificationType.valueOf(request.getType()) : NotificationType.SYSTEM)
                .actionUrl(request.getActionUrl())
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        logAudit("SEND_PRIVATE_NOTIF", "notifications", String.valueOf(notification.getId()), null, request);

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("publicId", notification.getPublicId());
        payload.put("title", notification.getTitle());
        payload.put("content", notification.getContent());
        payload.put("type", notification.getType() != null ? notification.getType().name() : null);
        payload.put("actionUrl", notification.getActionUrl());
        payload.put("isBroadcast", false);
        if (notification.getTemplate() != null) {
            payload.put("templateCode", notification.getTemplate().getCode());
            payload.put("params", notification.getParams());
        }

        // Bắn sự kiện qua Kafka cho WebSocket hoặc FCM
        kafkaTemplate.send("notification.private.send", user.getPublicId().toString(), payload);
    }

    @Transactional
    public void sendBroadcastNotification(SendBroadcastNotificationRequest request) {
        NotificationTemplate template = null;
        if (request.getTemplateCode() != null) {
            template = notificationTemplateRepository.findById(request.getTemplateCode())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found: " + request.getTemplateCode()));
        }

        BroadcastNotification notification = BroadcastNotification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .template(template)
                .params(request.getParams())
                .type(request.getType() != null ? NotificationType.valueOf(request.getType()) : NotificationType.SYSTEM)
                .actionUrl(request.getActionUrl())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        notification = broadcastNotificationRepository.save(notification);

        logAudit("SEND_BROADCAST_NOTIF", "broadcast_notifications", String.valueOf(notification.getId()), null, request);

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("publicId", notification.getPublicId());
        payload.put("title", notification.getTitle());
        payload.put("content", notification.getContent());
        payload.put("type", notification.getType() != null ? notification.getType().name() : null);
        payload.put("actionUrl", notification.getActionUrl());
        payload.put("isBroadcast", true);
        if (notification.getTemplate() != null) {
            payload.put("templateCode", notification.getTemplate().getCode());
            payload.put("params", notification.getParams());
        }

        // Bắn sự kiện qua Kafka để Push Realtime tới mọi user
        kafkaTemplate.send("notification.broadcast.send", "ALL", payload);
    }

    @Transactional(readOnly = true)
    public java.util.List<com.tony.demo.modules.notification.api.dto.NotificationTemplateDTO> getAllTemplates() {
        return notificationTemplateRepository.findAll().stream()
                .map(t -> new com.tony.demo.modules.notification.api.dto.NotificationTemplateDTO(
                        t.getCode(), t.getTitleEn(), t.getTitleVi(), t.getContentEn(), t.getContentVi()))
                .toList();
    }

    private void logAudit(String action, String targetEntity, String targetId, Object oldData, Object newData) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Staff staff = staffRepository.findByEmployeeCode(username).orElse(null);
        if (staff == null) {
            return;
        }

        try {
            AuditLog auditLog = AuditLog.builder()
                    .publicId(UUID.randomUUID())
                    .staff(staff)
                    .action(action)
                    .targetEntity(targetEntity)
                    .targetId(targetId)
                    .oldData(oldData != null ? objectMapper.writeValueAsString(oldData) : null)
                    .newData(newData != null ? objectMapper.writeValueAsString(newData) : null)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit log data", e);
        }
    }
}
