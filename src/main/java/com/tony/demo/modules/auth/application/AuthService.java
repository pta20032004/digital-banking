package com.tony.demo.modules.auth.application;

import java.util.Map;

import com.tony.demo.modules.auth.dto.AuthResponse;
import com.tony.demo.modules.auth.dto.LoginRequest;
import com.tony.demo.modules.auth.dto.RegisterRequest;
import com.tony.demo.modules.auth.dto.VerifyOtpRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse registerFull(RegisterRequest request, org.springframework.web.multipart.MultipartFile frontImage, org.springframework.web.multipart.MultipartFile backImage);
    Map<String, Object> login(LoginRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
    AuthResponse verifyEmailToken(String token);
}
