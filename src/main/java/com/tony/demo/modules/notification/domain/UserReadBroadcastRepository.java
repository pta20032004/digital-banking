package com.tony.demo.modules.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReadBroadcastRepository extends JpaRepository<UserReadBroadcast, UserReadBroadcastId> {
    boolean existsById(UserReadBroadcastId id);
}
