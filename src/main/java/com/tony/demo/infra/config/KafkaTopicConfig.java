package com.tony.demo.infra.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private static final int PARTITIONS = 3;
    private static final int REPLICAS = 1;

    // A. Phân hệ Giao dịch (Transaction Domain)
    @Bean
    public NewTopic transferRequestedTopic() {
        return TopicBuilder.name("tx.transfer.requested")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    @Bean
    public NewTopic transferSucceededTopic() {
        return TopicBuilder.name("tx.transfer.succeeded")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    @Bean
    public NewTopic transferFailedTopic() {
        return TopicBuilder.name("tx.transfer.failed")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    // B. Phân hệ Tài khoản & Số dư (Account & Balance)
    @Bean
    public NewTopic accountBalanceUpdatedTopic() {
        return TopicBuilder.name("account.balance.updated")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    @Bean
    public NewTopic accountStatusChangedTopic() {
        return TopicBuilder.name("account.status.changed")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    @Bean
    public NewTopic userKycVerifiedTopic() {
        return TopicBuilder.name("user.kyc.verified")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    // C. Phân hệ Bảo mật & Thông báo (Security & Notification)
    @Bean
    public NewTopic authOtpCreatedTopic() {
        return TopicBuilder.name("auth.otp.created")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    @Bean
    public NewTopic notificationPushCommandTopic() {
        return TopicBuilder.name("notification.push.command")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }
}
