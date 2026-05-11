package com.tony.demo.infra.elasticsearch;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tony.demo.modules.transaction.domain.Transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSyncService {

    private final TransactionSearchRepository searchRepository;

    @Async
    public void syncTransaction(Transaction transaction) {
        try {
            TransactionDocument doc = TransactionDocument.builder()
                    .id(transaction.getId().toString())
                    .transactionId(transaction.getId())
                    .fromAccountId(transaction.getFromAccountId())
                    .toAccountId(transaction.getToAccountId())
                    .amount(transaction.getAmount())
                    .status(transaction.getStatus().name())
                    .transactionType(transaction.getTransactionType().name())
                    .description(transaction.getDescription())
                    .createdAt(transaction.getCreatedAt())
                    .build();
            
            searchRepository.save(doc);
            log.info("Successfully synced transaction {} to Elasticsearch", transaction.getId());
        } catch (Exception e) {
            log.error("Error syncing transaction to Elasticsearch: ", e);
        }
    }
}
