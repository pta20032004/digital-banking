package com.tony.demo.modules.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface BroadcastNotificationRepository extends JpaRepository<BroadcastNotification, Long> {
    Optional<BroadcastNotification> findByPublicId(UUID publicId);
}
