package com.tony.demo.modules.notification.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReadBroadcastId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "broadcast_id")
    private Long broadcastId;
}
