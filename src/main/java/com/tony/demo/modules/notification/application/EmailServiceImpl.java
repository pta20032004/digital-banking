package com.tony.demo.modules.notification.application;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("banking@no-reply.com"); // Thay bằng email cấu hình của bạn
            message.setTo(toEmail);
            message.setSubject("Mã OTP Xác Thực Đăng Nhập");
            message.setText("Chào bạn,\n\nMã OTP xác thực đăng nhập của bạn là: " + otpCode + "\nMã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\nTrân trọng,\nĐội ngũ Digital Banking");

            mailSender.send(message);
            log.info("Đã gửi email chứa mã OTP thành công tới {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email chứa mã OTP tới {}: {}", toEmail, e.getMessage());
        }
    }
    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("banking@no-reply.com");
            message.setTo(toEmail);
            message.setSubject("Xác thực tài khoản Digital Banking");
            message.setText("Chào bạn,\n\nVui lòng click vào link sau để xác thực email của bạn:\n" 
                + "http://localhost:5173/register-success?token=" + token 
                + "\n\nTrân trọng,\nĐội ngũ Digital Banking");

            mailSender.send(message);
            log.info("Đã gửi email xác thực thành công tới {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác thực tới {}: {}", toEmail, e.getMessage());
        }
    }
}
