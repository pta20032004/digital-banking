package com.tony.demo.modules.auth.application;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String REDIS_OTP_PREFIX = "otp:";
    // 2 minutes 15 seconds = 135 seconds
    private static final long OTP_VALID_DURATION_SECONDS = 135;

    @Override
    public void sendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        // TODO: In tạm ra màn hình để test trong lúc chưa có Kafka Consumer gửi Email thật
        log.info("========== MÃ OTP DÀNH CHO EMAIL [{}] LÀ: {} ==========", email, otp);

        // Lưu OTP vào Redis với thời gian hết hạn
        redisTemplate.opsForValue().set(REDIS_OTP_PREFIX + email, otp, Duration.ofSeconds(OTP_VALID_DURATION_SECONDS));

        // Gửi sự kiện qua Kafka
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("otp", otp);
            payload.put("type", "OTP_VERIFICATION");

            kafkaTemplate.send("auth.otp.created", email, payload).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Đã xác nhận gửi sự kiện OTP qua Kafka thành công cho email: {}", email);
                } else {
                    log.error("Lỗi khi broker nhận sự kiện OTP qua Kafka cho email {}: {}", email, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi chuẩn bị gửi Kafka cho email {}: {}", email, e.getMessage());
            // Có thể throw exception ở đây nếu muốn dừng luồng xử lý
        }
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String key = REDIS_OTP_PREFIX + email;
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp != null && savedOtp.equals(otp)) {
            // Xóa OTP khỏi Redis sau khi xác minh thành công
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }
}
