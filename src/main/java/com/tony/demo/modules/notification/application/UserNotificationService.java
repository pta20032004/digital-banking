package com.tony.demo.modules.notification.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tony.demo.core.exception.AppException;
import com.tony.demo.core.enums.ErrorCode;
import com.tony.demo.modules.notification.api.dto.response.UserNotificationDTO;
import com.tony.demo.modules.notification.api.dto.response.UserNotificationResponse;
import com.tony.demo.modules.notification.domain.BroadcastNotification;
import com.tony.demo.modules.notification.domain.BroadcastNotificationRepository;
import com.tony.demo.modules.notification.domain.Notification;
import com.tony.demo.modules.notification.domain.NotificationRepository;
import com.tony.demo.modules.notification.domain.NotificationTemplate;
import com.tony.demo.modules.notification.domain.NotificationTemplateRepository;
import com.tony.demo.modules.notification.domain.UserNotificationProjection;
import com.tony.demo.modules.notification.domain.UserReadBroadcast;
import com.tony.demo.modules.notification.domain.UserReadBroadcastId;
import com.tony.demo.modules.notification.domain.UserReadBroadcastRepository;
import com.tony.demo.modules.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final NotificationRepository notificationRepository;
    private final BroadcastNotificationRepository broadcastNotificationRepository;
    private final UserReadBroadcastRepository userReadBroadcastRepository;
    private final NotificationTemplateRepository templateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Transactional(readOnly = true)
    public UserNotificationResponse getUserNotifications(User user, Pageable pageable, String acceptLanguage) {
        Page<UserNotificationProjection> page = notificationRepository.findAllUserNotificationsMerged(user.getId(), pageable);
        long unreadCount = notificationRepository.countUnreadMerged(user.getId());

        boolean isVietnamese = acceptLanguage != null && acceptLanguage.toLowerCase().contains("vi");

        List<UserNotificationDTO> dtoList = page.getContent().stream().map(proj -> {
            String title = proj.getTitle();
            String content = proj.getContent();

            if (StringUtils.hasText(proj.getTemplateCode())) {
                NotificationTemplate template = templateRepository.findById(proj.getTemplateCode()).orElse(null);
                if (template != null) {
                    title = isVietnamese ? template.getTitleVi() : template.getTitleEn();
                    content = isVietnamese ? template.getContentVi() : template.getContentEn();
                    
                    if (StringUtils.hasText(proj.getParams())) {
                        try {
                            Map<String, Object> params = objectMapper.readValue(proj.getParams(), new TypeReference<>() {});
                            for (Map.Entry<String, Object> entry : params.entrySet()) {
                                String key = "{" + entry.getKey() + "}";
                                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                                title = title.replace(key, value);
                                content = content.replace(key, value);
                            }
                        } catch (Exception e) {
                            // Ignore mapping error
                        }
                    }
                }
            }

            return UserNotificationDTO.builder()
                .publicId(proj.getPublicId())
                .title(title)
                .content(content)
                .type(proj.getType())
                .isRead(proj.getIsRead())
                .isBroadcast(proj.getIsBroadcast())
                .actionUrl(proj.getActionUrl())
                .createdAt(proj.getCreatedAt())
                .build();
        }).collect(Collectors.toList());

        return UserNotificationResponse.builder()
            .unreadCount(unreadCount)
            .data(dtoList)
            .build();
    }

    @Transactional
    public void markAsRead(User user, UUID publicId) {
        // We must check if publicId belongs to personal notification or broadcast
        Notification personalNotif = notificationRepository.findByPublicId(publicId).orElse(null);
        if (personalNotif != null) {
            if (!personalNotif.getUser().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
            if (!personalNotif.getIsRead()) {
                personalNotif.setIsRead(true);
                personalNotif.setReadAt(Instant.now());
                notificationRepository.save(personalNotif);
            }
            return;
        }

        BroadcastNotification broadcastNotif = broadcastNotificationRepository.findByPublicId(publicId).orElse(null);
        if (broadcastNotif != null) {
            UserReadBroadcastId id = new UserReadBroadcastId(user.getId(), broadcastNotif.getId());
            if (!userReadBroadcastRepository.existsById(id)) {
                UserReadBroadcast userRead = UserReadBroadcast.builder()
                    .id(id)
                    .user(user)
                    .broadcast(broadcastNotif)
                    .readAt(Instant.now())
                    .build();
                userReadBroadcastRepository.save(userRead);
            }
            return;
        }

        throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
