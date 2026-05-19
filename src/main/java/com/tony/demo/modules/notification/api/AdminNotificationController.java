package com.tony.demo.modules.notification.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tony.demo.modules.notification.api.dto.SendBroadcastNotificationRequest;
import com.tony.demo.modules.notification.api.dto.SendPrivateNotificationRequest;
import com.tony.demo.modules.notification.application.AdminNotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @PostMapping("/private")
    @PreAuthorize("hasAuthority('NOTI_SEND_DIRECT')")
    public ResponseEntity<Void> sendPrivateNotification(@Validated @RequestBody SendPrivateNotificationRequest request) {
        adminNotificationService.sendPrivateNotification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasAuthority('NOTI_SEND_BROADCAST')")
    public ResponseEntity<Void> sendBroadcastNotification(@Validated @RequestBody SendBroadcastNotificationRequest request) {
        adminNotificationService.sendBroadcastNotification(request);
        return ResponseEntity.ok().build();
    }
}
