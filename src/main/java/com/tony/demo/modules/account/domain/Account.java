package com.tony.demo.modules.account.domain;

import java.math.BigDecimal;

import com.tony.demo.core.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import lombok.experimental.SuperBuilder;

@Table(name = "accounts")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Account extends BaseEntity {
    
    @Column(name = "user_id")
    @NotNull
    private Long userId;

    @Column(name = "account_number")
    @NotNull 
    @Size(max = 20)
    private String accountNumber;

    @Column(name = "balance")
    @NotNull
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "version")
    @NotNull
    @Builder.Default
    @Version
    private Integer version = 0;

    @Column(name = "currency")
    @NotNull
    @Builder.Default
    private String currency = "VND";

    @Column(name = "status", columnDefinition = "account_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    
}
