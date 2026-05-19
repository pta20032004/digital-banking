package com.tony.demo.modules.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tony.demo.modules.auth.application.AuthService;
import com.tony.demo.modules.auth.application.OtpService;
import com.tony.demo.modules.auth.dto.AuthResponse;
import com.tony.demo.modules.auth.dto.LoginRequest;
import com.tony.demo.modules.auth.dto.RegisterRequest;
import com.tony.demo.modules.auth.dto.SendOtpRequest;
import com.tony.demo.modules.auth.dto.VerifyOtpRequest;

import java.util.Map;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Validated RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Validated LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/manager-login")
    public ResponseEntity<AuthResponse> loginManager(@RequestBody @Validated LoginRequest request) {
        return ResponseEntity.ok(authService.loginManager(request));
    }

//    @PostMapping("/send-otp")
//    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody @Validated SendOtpRequest request) {
//        otpService.sendOtp(request.getEmail());
//        return ResponseEntity.ok(Map.of("message", "Mã OTP đã được gửi thành công."));
//    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody @Validated VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }
    @PostMapping(value = "/register-full", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> registerFull(
            @org.springframework.web.bind.annotation.RequestPart("request") @Validated RegisterRequest request,
            @org.springframework.web.bind.annotation.RequestPart("frontImage") org.springframework.web.multipart.MultipartFile frontImage,
            @org.springframework.web.bind.annotation.RequestPart("backImage") org.springframework.web.multipart.MultipartFile backImage) {
        return ResponseEntity.ok(authService.registerFull(request, frontImage, backImage));
    }

    @org.springframework.web.bind.annotation.GetMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@org.springframework.web.bind.annotation.RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmailToken(token));
    }
}
