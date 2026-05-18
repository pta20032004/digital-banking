package com.tony.demo.modules.transaction.application;

import com.tony.demo.core.exception.AppException;
import com.tony.demo.core.enums.ErrorCode;
import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.account.domain.AccountStatus;
import com.tony.demo.modules.transaction.domain.Transaction;
import com.tony.demo.modules.transaction.domain.TransactionRepository;
import com.tony.demo.modules.transaction.domain.TransactionStatus;
import com.tony.demo.modules.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountTransferExecutor {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void processCoreTransfer(Long senderId, Long receiverId, BigDecimal amount, String description) {
        if (senderId.equals(receiverId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 1. SELECT FOR UPDATE VỚI ORDERED IDS (Anti-Deadlock)
        // Việc sắp xếp id tăng dần ở Repository query giúp khóa row theo đúng thứ tự (A -> B hoặc B -> A luôn nhất quán)
        List<Account> accounts = accountRepository.findByIdsForUpdate(List.of(senderId, receiverId));
        if (accounts.size() != 2) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        Account sender = accounts.stream().filter(a -> a.getId().equals(senderId)).findFirst().orElseThrow();
        Account receiver = accounts.stream().filter(a -> a.getId().equals(receiverId)).findFirst().orElseThrow();

        // 2. NGHIỆP VỤ (BUSINESS CHECKS)
        if (sender.getStatus() != AccountStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);
        }
        if (receiver.getStatus() != AccountStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);
        }
        
        // Kiểm tra số dư an toàn trong Lock
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 3. UPDATE BALANCES
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 4. LƯU LỊCH SỬ GIAO DỊCH
        Transaction transaction = Transaction.builder()
                .publicId(UUID.randomUUID())
                .fromAccountId(senderId)
                .toAccountId(receiverId)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description(description)
                .fromPostBalance(sender.getBalance())
                .toPostBalance(receiver.getBalance())
                .build();
                
        transactionRepository.save(transaction);
    }
}
