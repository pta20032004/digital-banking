package com.tony.demo.modules.notification.api;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tony.demo.modules.notification.api.dto.response.UserNotificationResponse;
import com.tony.demo.modules.notification.application.UserNotificationService;
import com.tony.demo.modules.user.domain.User;

import lombok.RequiredArgsConstructor;

import com.tony.demo.modules.user.domain.UserRepository;
import com.tony.demo.core.exception.AppException;
import com.tony.demo.core.enums.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<UserNotificationResponse> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Accept-Language", defaultValue = "vi-VN") String acceptLanguage) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        UserNotificationResponse response = userNotificationService.getUserNotifications(user, pageable, acceptLanguage);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{publicId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID publicId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
        userNotificationService.markAsRead(user, publicId);
        
        return ResponseEntity.ok().build();
    }
}
