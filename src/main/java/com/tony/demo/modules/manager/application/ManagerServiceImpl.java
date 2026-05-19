package com.tony.demo.modules.manager.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.user.domain.UserRepository;
import com.tony.demo.modules.user.domain.UserStatus;
import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.account.domain.AccountStatus;
import com.tony.demo.modules.transaction.domain.Transaction;
import com.tony.demo.modules.transaction.domain.TransactionRepository;
import com.tony.demo.core.exception.AppException;
import com.tony.demo.core.enums.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void approveKyc(String identifier) {
        User user = userRepository.findByUsername(identifier).orElse(null);
        if (user == null) {
            Account account = accountRepository.findByAccountNumber(identifier)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
            user = userRepository.findById(account.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }
        
        user.setKycStatus(UserStatus.VERIFIED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void lockUser(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
    }

    @Override
    public User getUserByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        return userRepository.findById(account.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket.kyc:kyc-bucket}")
    private String kycBucket;

    private final com.tony.demo.service.S3Service s3Service;

    @Override
    public java.util.List<com.tony.demo.modules.manager.api.dto.PendingKycUserResponse> getPendingKycUsers() {
        java.util.List<User> pendingUsers = userRepository.findAllByKycStatus(UserStatus.PENDING);
        
        return pendingUsers.stream().map(user -> {
            String prefix = user.getUsername() + "_";
            java.util.List<String> files = s3Service.listFilesByPrefix(kycBucket, prefix);
            
            String frontKey = files.stream().filter(f -> f.endsWith("_front")).max(String::compareTo).orElse(null);
            String backKey = files.stream().filter(f -> f.endsWith("_back")).max(String::compareTo).orElse(null);
            
            String frontUrl = frontKey != null ? s3Service.generatePresignedUrl(kycBucket, frontKey, java.time.Duration.ofMinutes(60)) : null;
            String backUrl = backKey != null ? s3Service.generatePresignedUrl(kycBucket, backKey, java.time.Duration.ofMinutes(60)) : null;
            
            java.util.List<Account> accounts = accountRepository.findByUserId(user.getId());
            String accountNumber = accounts.isEmpty() ? null : accounts.get(0).getAccountNumber();
            
            return com.tony.demo.modules.manager.api.dto.PendingKycUserResponse.builder()
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .identityNumber(user.getIdentityNumber())
                    .dateOfBirth(user.getDateOfBirth())
                    .accountNumber(accountNumber)
                    .frontImageUrl(frontUrl)
                    .backImageUrl(backUrl)
                    .build();
        }).collect(java.util.stream.Collectors.toList());
    }
}
