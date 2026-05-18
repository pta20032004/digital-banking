package com.tony.demo.modules.notification.application;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode);
    void sendVerificationEmail(String toEmail, String token);
}
