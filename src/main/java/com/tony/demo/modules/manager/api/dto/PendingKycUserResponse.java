package com.tony.demo.modules.manager.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PendingKycUserResponse {
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String identityNumber;
    private LocalDate dateOfBirth;
    private String accountNumber;
    private String frontImageUrl;
    private String backImageUrl;
    private String status;
    private String kycStatus;
    private java.math.BigDecimal balance;
    private java.util.List<com.tony.demo.modules.transaction.domain.Transaction> recentTransactions;
}
