package com.tony.demo.modules.auth.application;

public interface OtpService {
    void sendOtp(String email);
    boolean verifyOtp(String email, String otp);
}
