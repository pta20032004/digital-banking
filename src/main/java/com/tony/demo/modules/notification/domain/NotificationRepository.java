package com.tony.demo.modules.notification.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByPublicId(UUID publicId);
    
    @Query(value = """
        SELECT * FROM (
            SELECT CAST(n.public_id AS VARCHAR) as publicId, n.title, n.content, n.type, 
                   n.is_read as isRead, false as isBroadcast, n.action_url as actionUrl, 
                   n.created_at as createdAt, CAST(n.params AS VARCHAR) as params, n.template_code as templateCode
            FROM notifications n 
            WHERE n.user_id = :userId
            
            UNION ALL
            
            SELECT CAST(b.public_id AS VARCHAR) as publicId, b.title, b.content, b.type,
                   CASE WHEN ur.read_at IS NOT NULL THEN true ELSE false END as isRead,
                   true as isBroadcast, b.action_url as actionUrl, 
                   b.created_at as createdAt, CAST(b.params AS VARCHAR) as params, b.template_code as templateCode
            FROM broadcast_notifications b
            LEFT JOIN user_read_broadcasts ur ON b.id = ur.broadcast_id AND ur.user_id = :userId
            WHERE b.start_time <= CURRENT_TIMESTAMP 
              AND (b.end_time IS NULL OR b.end_time >= CURRENT_TIMESTAMP)
        ) as combined_notifications
        ORDER BY createdAt DESC
    """, countQuery = """
        SELECT COUNT(*) FROM (
            SELECT n.id FROM notifications n WHERE n.user_id = :userId
            UNION ALL
            SELECT b.id FROM broadcast_notifications b
            WHERE b.start_time <= CURRENT_TIMESTAMP 
              AND (b.end_time IS NULL OR b.end_time >= CURRENT_TIMESTAMP)
        ) as combined_counts
    """, nativeQuery = true)
    Page<UserNotificationProjection> findAllUserNotificationsMerged(@Param("userId") Long userId, Pageable pageable);
    
    @Query(value = """
        SELECT 
            (SELECT COUNT(*) FROM notifications n WHERE n.user_id = :userId AND n.is_read = false) +
            (SELECT COUNT(*) FROM broadcast_notifications b 
             LEFT JOIN user_read_broadcasts ur ON b.id = ur.broadcast_id AND ur.user_id = :userId
             WHERE b.start_time <= CURRENT_TIMESTAMP 
               AND (b.end_time IS NULL OR b.end_time >= CURRENT_TIMESTAMP) 
               AND ur.read_at IS NULL)
    """, nativeQuery = true)
    long countUnreadMerged(@Param("userId") Long userId);
}

