package com.tony.demo.modules.transaction.application;

import com.tony.demo.core.exception.AppException;
import com.tony.demo.core.enums.ErrorCode;
import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.transaction.api.dto.request.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tony.demo.modules.transaction.api.dto.response.TransactionHistoryResponse;
import com.tony.demo.modules.user.domain.UserRepository;
import com.tony.demo.modules.transaction.domain.TransactionRepository;
import com.tony.demo.modules.transaction.domain.Transaction;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountTransferExecutor accountTransferExecutor;
    private final AccountRepository accountRepository;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    private static final String IDEMPOTENCY_PREFIX = "req_id:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:user:";

    @Override
    public void executeTransfer(TransferRequest request) {
        
        // 0. BẢO MẬT: Phân giải Account Number thành Internal ID (Long) để chống IDOR
        Account sender = accountRepository.findByAccountNumber(request.getSenderAccountNumber())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account receiver = accountRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        Long senderInternalId = sender.getId();
        Long receiverInternalId = receiver.getId();

        // 1. RATE LIMITING (Giới hạn 2 requests / giây / user)
        String rateLimitKey = RATE_LIMIT_PREFIX + senderInternalId;
        Long reqCount = redisTemplate.opsForValue().increment(rateLimitKey);
        if (reqCount != null && reqCount == 1) {
            redisTemplate.expire(rateLimitKey, Duration.ofSeconds(1));
        }
        if (reqCount != null && reqCount > 2) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 2. IDEMPOTENCY (Chống trùng lặp / Double click)
        String idempotencyKey = IDEMPOTENCY_PREFIX + request.getRequestId();
        Boolean isNewRequest = redisTemplate.opsForValue()
                .setIfAbsent(idempotencyKey, "PROCESSING", Duration.ofMinutes(5));
        
        if (Boolean.FALSE.equals(isNewRequest)) {
            log.warn("Duplicate request detected: {}", request.getRequestId());
            throw new AppException(ErrorCode.DUPLICATE_TRANSACTION);
        }

        try {
            // 3. THỰC THI GIAO DỊCH CORE BÊN TRONG TRANSACTION (THÔNG QUA PROXY)
            accountTransferExecutor.processCoreTransfer(
                senderInternalId, 
                receiverInternalId, 
                request.getAmount(), 
                request.getDescription()
            );

            // 4. CẬP NHẬT TRẠNG THÁI IDEMPOTENCY
            redisTemplate.opsForValue().set(idempotencyKey, "SUCCESS", Duration.ofMinutes(5));

            // 5. CACHE EVICT & ASYNC TASKS (KAFKA)
            redisTemplate.delete("balance_cache:user:" + senderInternalId);
            redisTemplate.delete("balance_cache:user:" + receiverInternalId);
            
            // Push sự kiện Kafka để gửi email/thông báo bất đồng bộ
            kafkaTemplate.send("transfer_events", "Transfer successful from " + senderInternalId + " to " + receiverInternalId + " amount " + request.getAmount());

        } catch (Exception e) {
            log.error("Transfer failed for request {}: {}", request.getRequestId(), e.getMessage());
            // Xóa khóa idempotency nếu có lỗi để cho phép thử lại (tuỳ logic business, nếu lỗi logic thì có thể set trạng thái FAILED)
            redisTemplate.delete(idempotencyKey);
            throw e;
        }
    }

    @Override
    public Page<TransactionHistoryResponse> getTransactionHistory(String username, Pageable pageable) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var accounts = accountRepository.findByUserId(user.getId());
        if (accounts.isEmpty()) {
            return Page.empty(pageable);
        }
        Long mainAccountId = accounts.get(0).getId();

        Page<Transaction> transactions = transactionRepository.findTransactionHistory(mainAccountId, pageable);

        return transactions.map(t -> {
            BigDecimal fee = BigDecimal.ZERO; // For simplicity, assume 0 fee or fetch if present
            String transactionType = java.util.Objects.equals(t.getFromAccountId(), mainAccountId) ? "OUT" : "IN";
            java.time.LocalDateTime transactionTime = t.getCreatedAt() != null 
                    ? java.time.LocalDateTime.ofInstant(t.getCreatedAt(), java.time.ZoneId.systemDefault()) 
                    : null;
            return new TransactionHistoryResponse(
                    t.getPublicId(),
                    transactionType,
                    t.getAmount(),
                    fee,
                    t.getStatus().name(),
                    transactionTime,
                    t.getDescription()
            );
        });
    }
}
