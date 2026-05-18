package com.tony.demo.modules.user.domain;

import com.tony.demo.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Entity
@Table(name = "beneficiaries", uniqueConstraints = {
    @UniqueConstraint(name = "unique_user_beneficiary", columnNames = {"user_id", "account_number", "bank_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Beneficiary extends BaseEntity {

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "bank_code", nullable = false, length = 50)
    @Builder.Default
    private String bankCode = "INTERNAL";

    @Column(name = "remind_name", nullable = false, length = 255)
    private String remindName;

    @PrePersist
    public void generatePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }
}
