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
    public void approveKyc(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        User user = userRepository.findById(account.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
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
}
