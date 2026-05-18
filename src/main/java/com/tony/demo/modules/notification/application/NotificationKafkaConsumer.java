package com.tony.demo.modules.notification.application;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final EmailService emailService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("========== NotificationKafkaConsumer BEAN ĐÃ ĐƯỢC KHỞI TẠO VÀ SẴN SÀNG LẮNG NGHE KAFKA ==========");
    }

    @KafkaListener(topics = "auth.otp.created", groupId = "${spring.kafka.consumer.group-id:banking-group}")
    public void consumeOtpEvent(String payloadStr) {
        log.info("Nhận được chuỗi sự kiện Kafka từ topic 'auth.otp.created': {}", payloadStr);
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> payload = objectMapper.readValue(payloadStr, Map.class);
            String email = payload.get("email");
            String otp = payload.get("otp");
            String type = payload.get("type");

            if ("OTP_VERIFICATION".equals(type) && email != null && otp != null) {
                log.info("Tiến hành gửi email chứa OTP tới: {}", email);
                emailService.sendOtpEmail(email, otp);
            } else {
                log.warn("Dữ liệu sự kiện Kafka không hợp lệ hoặc thiếu thông tin: {}", payload);
            }
        } catch (Exception e) {
            log.error("Có lỗi xảy ra khi phân tích/xử lý sự kiện Kafka gửi OTP: {}", e.getMessage(), e);
        }
    }
}
