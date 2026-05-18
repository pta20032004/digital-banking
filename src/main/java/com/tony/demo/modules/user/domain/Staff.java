package com.tony.demo.modules.user.domain;

import java.time.Instant;

import com.tony.demo.core.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "staffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Staff extends BaseEntity {

    @Column(name = "employee_code", unique = true, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String employeeCode;

    @Column(name = "full_name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank
    @Email
    private String email;

    @Column(name = "password", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @NotNull
    private Role role;

    @Column(name = "department")
    @Size(max = 100)
    private String department;

    @Column(name = "branch_code")
    @Size(max = 50)
    private String branchCode;

    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "is_locked")
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "failed_attempt")
    @Builder.Default
    private Integer failedAttempt = 0;

    @Column(name = "lock_time")
    private Instant lockTime;
}
