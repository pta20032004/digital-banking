package com.tony.demo.infra.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.user.domain.Notification;
import com.tony.demo.modules.user.domain.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;

    @KafkaListener(topics = "transaction-events", groupId = "digital-banking-group")
    @Transactional
    public void consumeTransactionEvent(TransactionEventProducer.TransactionEvent event) {
        log.info("Received Transaction Event: {}", event);

        // Find user by accountId
        Account account = accountRepository.findById(event.getAccountId()).orElse(null);
        if (account == null) {
            log.warn("Account not found for ID: {}", event.getAccountId());
            return;
        }

        String title = "Transaction Alert";
        String content = String.format("A transaction of %s has been processed for your account %s. Type: %s",
                event.getAmount(), account.getAccountNumber(), event.getType());

        Notification notification = Notification.builder()
                .userId(account.getUserId())
                .title(title)
                .content(content)
                .type("TRANSACTION_ALERT")
                .build();

        notificationRepository.save(notification);

        // Simulate pushing to mobile app
        log.info("Push Notification sent to User {}: {}", account.getUserId(), content);
    }
}
