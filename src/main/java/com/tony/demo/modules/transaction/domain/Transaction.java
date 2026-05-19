package com.tony.demo.modules.transaction.domain;

import java.math.BigDecimal;

import com.tony.demo.core.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table(name = "transactions")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Transaction extends BaseEntity {
    
    @Column(name = "from_account_id")
    // Bỏ @NotNull để hỗ trợ DEPOSIT: gửi tiền
    private Long fromAccountId;

    @Column(name = "to_account_id")
    // Bỏ @NotNull để hỗ trợ WITHDRAW:rút tiền
    private Long toAccountId;

    @Column(name = "amount")
    @NotNull
    @Positive
    private BigDecimal amount;

    @Column(name = "fee")
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "status", columnDefinition = "transaction_status")
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_type", columnDefinition = "transaction_type")
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @NotNull
    private TransactionType transactionType;

    @Column(name = "description")
    private String description;

    @Column(name = "from_post_balance")
    private BigDecimal fromPostBalance;

    @Column(name = "to_post_balance")
    private BigDecimal toPostBalance;
}
