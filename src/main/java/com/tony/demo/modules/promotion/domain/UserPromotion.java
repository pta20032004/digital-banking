package com.tony.demo.modules.promotion.domain;

import com.tony.demo.core.model.BaseEntity;
import com.tony.demo.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.ZonedDateTime;

@Entity
@Table(name = "user_promotions", uniqueConstraints = {
    @UniqueConstraint(name = "unique_user_promo", columnNames = {"user_id", "promotion_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserPromotion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "promo_status")
    @Builder.Default
    private PromoStatus status = PromoStatus.CLAIMED;

    @Column(name = "used_at")
    private ZonedDateTime usedAt;
}
