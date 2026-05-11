package com.tony.demo.modules.transaction.application;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tony.demo.core.exception.BankingException;
import com.tony.demo.core.exception.ErrorCode;
import com.tony.demo.infra.kafka.TransactionEventProducer;
import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.transaction.domain.Transaction;
import com.tony.demo.modules.transaction.domain.TransactionRepository;
import com.tony.demo.modules.transaction.domain.TransactionStatus;
import com.tony.demo.modules.transaction.domain.TransactionType;
import com.tony.demo.modules.transaction.application.TransactionDto.TransactionRequest;
import com.tony.demo.modules.transaction.application.TransactionDto.TransactionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final StringRedisTemplate redisTemplate;
    private final TransactionEventProducer transactionEventProducer;
    private final com.tony.demo.infra.elasticsearch.TransactionSyncService transactionSyncService;

    private static final String IDEMPOTENCY_PREFIX = "idem:req:";
    private static final long IDEMPOTENCY_TTL_SECONDS = 30;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse transfer(TransactionRequest request) {
        checkIdempotency(request.getRequestId());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException(ErrorCode.INVALID_AMOUNT, "Transfer amount must be positive");
        }

        // Lock accounts in a consistent order to prevent deadlock
        Account firstLock, secondLock;
        boolean sourceIsFirst = request.getSourceAccountNumber().compareTo(request.getTargetAccountNumber()) < 0;

        if (sourceIsFirst) {
            firstLock = getAccountWithLock(request.getSourceAccountNumber());
            secondLock = getAccountWithLock(request.getTargetAccountNumber());
        } else {
            secondLock = getAccountWithLock(request.getTargetAccountNumber());
            firstLock = getAccountWithLock(request.getSourceAccountNumber());
        }

        Account sourceAccount = sourceIsFirst ? firstLock : secondLock;
        Account targetAccount = sourceIsFirst ? secondLock : firstLock;

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BankingException(ErrorCode.INSUFFICIENT_BALANCE, "Insufficient balance");
        }

        // Deduct from source
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        
        // Add to target
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        Transaction transaction = Transaction.builder()
                .fromAccountId(sourceAccount.getId())
                .toAccountId(targetAccount.getId())
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .fromPostBalance(sourceAccount.getBalance())
                .toPostBalance(targetAccount.getBalance())
                .build();

        transactionRepository.save(transaction);

        // Sync to Elasticsearch
        transactionSyncService.syncTransaction(transaction);

        // Send Kafka event
        transactionEventProducer.sendTransactionSuccessEvent(transaction.getId(), sourceAccount.getId(), request.getAmount(), "TRANSFER_OUT");
        transactionEventProducer.sendTransactionSuccessEvent(transaction.getId(), targetAccount.getId(), request.getAmount(), "TRANSFER_IN");

        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getId());
        response.setStatus(transaction.getStatus().name());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(transaction.getTransactionType().name());
        response.setMessage("Transfer successful");

        return response;
    }

    private void checkIdempotency(String requestId) {
        if (requestId != null && !requestId.trim().isEmpty()) {
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(IDEMPOTENCY_PREFIX + requestId, "PROCESSING", IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(isNew)) {
                throw new BankingException(ErrorCode.IDEMPOTENT_REQUEST_DUPLICATED, "Request is already processed or processing");
            }
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse deposit(TransactionRequest request) {
        checkIdempotency(request.getRequestId());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException(ErrorCode.INVALID_AMOUNT, "Deposit amount must be positive");
        }

        Account account = getAccountWithLock(request.getTargetAccountNumber());
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .toAccountId(account.getId())
                .amount(request.getAmount())
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .toPostBalance(account.getBalance())
                .build();

        transactionRepository.save(transaction);
        transactionSyncService.syncTransaction(transaction);
        transactionEventProducer.sendTransactionSuccessEvent(transaction.getId(), account.getId(), request.getAmount(), "DEPOSIT");

        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getId());
        response.setStatus(transaction.getStatus().name());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(transaction.getTransactionType().name());
        response.setMessage("Deposit successful");

        return response;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse withdraw(TransactionRequest request) {
        checkIdempotency(request.getRequestId());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException(ErrorCode.INVALID_AMOUNT, "Withdraw amount must be positive");
        }

        Account account = getAccountWithLock(request.getSourceAccountNumber());
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BankingException(ErrorCode.INSUFFICIENT_BALANCE, "Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .fromAccountId(account.getId())
                .amount(request.getAmount())
                .transactionType(TransactionType.WITHDRAW)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .fromPostBalance(account.getBalance())
                .build();

        transactionRepository.save(transaction);
        transactionSyncService.syncTransaction(transaction);
        transactionEventProducer.sendTransactionSuccessEvent(transaction.getId(), account.getId(), request.getAmount(), "WITHDRAW");

        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getId());
        response.setStatus(transaction.getStatus().name());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(transaction.getTransactionType().name());
        response.setMessage("Withdraw successful");

        return response;
    }

    private Account getAccountWithLock(String accountNumber) {
        return accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new BankingException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found: " + accountNumber));
    }
}
