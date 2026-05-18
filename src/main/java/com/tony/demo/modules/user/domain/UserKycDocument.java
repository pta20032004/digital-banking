package com.tony.demo.modules.user.domain;

import com.tony.demo.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_kyc_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserKycDocument extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "id_card_front_url", nullable = false, length = 500)
    private String idCardFrontUrl;

    @Column(name = "id_card_back_url", nullable = false, length = 500)
    private String idCardBackUrl;

    @Column(name = "selfie_url", nullable = false, length = 500)
    private String selfieUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Staff reviewedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}
