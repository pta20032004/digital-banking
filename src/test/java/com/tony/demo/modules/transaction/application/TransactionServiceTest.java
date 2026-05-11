package com.tony.demo.modules.transaction.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.tony.demo.core.exception.BankingException;
import com.tony.demo.core.exception.ErrorCode;
import com.tony.demo.infra.elasticsearch.TransactionSyncService;
import com.tony.demo.infra.kafka.TransactionEventProducer;
import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.transaction.application.TransactionDto.TransactionRequest;
import com.tony.demo.modules.transaction.application.TransactionDto.TransactionResponse;
import com.tony.demo.modules.transaction.domain.Transaction;
import com.tony.demo.modules.transaction.domain.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private TransactionEventProducer transactionEventProducer;

    @Mock
    private TransactionSyncService transactionSyncService;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account targetAccount;
    private TransactionRequest request;

    @BeforeEach
    void setUp() {
        sourceAccount = Account.builder()
                .id(1L)
                .accountNumber("123456789")
                .balance(new BigDecimal("1000.00"))
                .build();

        targetAccount = Account.builder()
                .id(2L)
                .accountNumber("987654321")
                .balance(new BigDecimal("500.00"))
                .build();

        request = new TransactionRequest();
        request.setRequestId("req-123");
        request.setSourceAccountNumber("123456789");
        request.setTargetAccountNumber("987654321");
        request.setAmount(new BigDecimal("200.00"));
        request.setDescription("Test Transfer");
    }

    @Test
    void transfer_Successful() {
        // Mock Redis Idempotency
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // Mock Account Repository
        when(accountRepository.findByAccountNumberWithLock("123456789")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberWithLock("987654321")).thenReturn(Optional.of(targetAccount));

        Transaction mockedTransaction = new Transaction();
        mockedTransaction.setId(10L);
        mockedTransaction.setAmount(new BigDecimal("200.00"));
        mockedTransaction.setStatus(com.tony.demo.modules.transaction.domain.TransactionStatus.SUCCESS);
        mockedTransaction.setTransactionType(com.tony.demo.modules.transaction.domain.TransactionType.TRANSFER);
        
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockedTransaction);

        TransactionResponse response = transactionService.transfer(request);

        assertNotNull(response);
        assertEquals("Transfer successful", response.getMessage());
        assertEquals(new BigDecimal("800.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), targetAccount.getBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(transactionSyncService, times(1)).syncTransaction(any());
        verify(transactionEventProducer, times(2)).sendTransactionSuccessEvent(any(), any(), any(), anyString());
    }

    @Test
    void transfer_InsufficientBalance_ThrowsException() {
        // Mock Redis
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        request.setAmount(new BigDecimal("2000.00"));

        when(accountRepository.findByAccountNumberWithLock("123456789")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberWithLock("987654321")).thenReturn(Optional.of(targetAccount));

        BankingException exception = assertThrows(BankingException.class, () -> transactionService.transfer(request));

        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        
        // Ensure nothing was saved
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_DuplicateRequest_ThrowsException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        BankingException exception = assertThrows(BankingException.class, () -> transactionService.transfer(request));

        assertEquals(ErrorCode.IDEMPOTENT_REQUEST_DUPLICATED, exception.getErrorCode());
    }
}
