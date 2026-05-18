package com.tony.demo.modules.user.domain;

import java.time.LocalDate;


import com.tony.demo.core.model.BaseEntity;
import com.tony.demo.modules.user.validation.ValidContactInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ValidContactInfo
public class User extends BaseEntity {

    //Info of user
    @Column(name = "full_name")
    @Size(max = 255)
    @NotBlank
    private String fullName;

    @Column(name = "date_of_birth")
    @NotNull
    private LocalDate dateOfBirth;

    @Column(name = "username", unique = true)
    @NotBlank
    @Size(max = 50)
    private String username;

    @Column(name = "email", unique = true)
    @Size(max = 100)
    @Email
    private String email;

    @Column(name = "password") 
    @Size(max = 255)
    private String password;

    @Column(name = "phone_number", unique = true)
    @Size(max = 20)
    private String phoneNumber;

    @Column(name = "identity_number")
    @NotNull
    @Size(max = 50)
    private String identityNumber;

    @Column(name = "kyc_status", columnDefinition = "user_status")
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private UserStatus kycStatus = UserStatus.PENDING;

    @Column(name = "transaction_pin")
    @Size(max = 255)
    private String transactionPin;
}
