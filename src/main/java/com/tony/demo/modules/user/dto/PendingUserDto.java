package com.tony.demo.modules.user.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingUserDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String identityNumber;
    private LocalDate dateOfBirth;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String selfieUrl;
}
