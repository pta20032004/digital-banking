package com.tony.demo.infra.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "transaction-events";

    public void sendTransactionSuccessEvent(Long transactionId, Long accountId, BigDecimal amount, String type) {
        TransactionEvent event = new TransactionEvent(transactionId, accountId, amount, type);
        kafkaTemplate.send(TOPIC, transactionId.toString(), event);
    }

    @Data
    @AllArgsConstructor
    public static class TransactionEvent {
        private Long transactionId;
        private Long accountId;
        private BigDecimal amount;
        private String type;
    }
}
