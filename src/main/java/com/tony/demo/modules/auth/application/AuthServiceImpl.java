package com.tony.demo.modules.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tony.demo.core.exception.AppException;
import com.tony.demo.core.enums.ErrorCode;
import com.tony.demo.modules.auth.dto.AuthResponse;
import com.tony.demo.modules.auth.dto.LoginRequest;
import com.tony.demo.modules.auth.dto.RegisterRequest;
import com.tony.demo.modules.user.domain.Role;
import com.tony.demo.modules.user.domain.RoleRepository;
import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.user.domain.UserRepository;
import com.tony.demo.modules.user.domain.UserStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final com.tony.demo.modules.account.domain.AccountRepository accountRepository;
    private final com.tony.demo.modules.user.domain.UserKycDocumentRepository userKycDocumentRepository;
    private final com.tony.demo.service.S3Service s3Service;
    private final com.tony.demo.modules.notification.application.EmailService emailService;
    private final com.tony.demo.modules.user.domain.StaffRepository staffRepository;

    @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket.kyc}")
    private String kycBucketName;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByIdentityNumber(request.identityNumber())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = User.builder()
                .fullName(request.fullName())
                .dateOfBirth(request.dateOfBirth())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .identityNumber(request.identityNumber())
                .transactionPin(passwordEncoder.encode(request.transactionPin()))
                .kycStatus(UserStatus.PENDING)
                .build();

        userRepository.save(user);

        return new AuthResponse(null, user.getUsername(), "User registered successfully");
    }

    @Override
    @Transactional
    public AuthResponse registerFull(RegisterRequest request, org.springframework.web.multipart.MultipartFile frontImage, org.springframework.web.multipart.MultipartFile backImage) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByIdentityNumber(request.identityNumber())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = User.builder()
                .fullName(request.fullName())
                .dateOfBirth(request.dateOfBirth())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .identityNumber(request.identityNumber())
                .transactionPin(passwordEncoder.encode(request.transactionPin()))
                .kycStatus(UserStatus.PENDING)
                .build();
        userRepository.save(user);

        // Upload images
        String timestamp = String.valueOf(System.currentTimeMillis());
        String frontFileName = request.username() + "_" + timestamp + "_front";
        String backFileName = request.username() + "_" + timestamp + "_back";
        
        try {
            s3Service.uploadFileWithExactName(kycBucketName, frontFileName, frontImage);
            s3Service.uploadFileWithExactName(kycBucketName, backFileName, backImage);
        } catch (java.io.IOException e) {
            log.error("Failed to upload KYC images", e);
            throw new RuntimeException("Lỗi khi tải ảnh CCCD lên hệ thống", e);
        }

        // Save KYC Document
        com.tony.demo.modules.user.domain.UserKycDocument kycDoc = com.tony.demo.modules.user.domain.UserKycDocument.builder()
                .user(user)
                .idCardFrontUrl(frontFileName)
                .idCardBackUrl(backFileName)
                .selfieUrl("") // required by DB schema but not in request
                .build();
        userKycDocumentRepository.save(kycDoc);

        // Create VND Account
        com.tony.demo.modules.account.domain.Account account = com.tony.demo.modules.account.domain.Account.builder()
                .userId(user.getId())
                .accountNumber(java.util.UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase()) // Random 10-char
                .balance(java.math.BigDecimal.valueOf(1000000))
                .currency("VND")
                .status(com.tony.demo.modules.account.domain.AccountStatus.ACTIVE)
                .build();
        accountRepository.save(account);

        // Send Email Verification
        String token = java.util.UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("verifyEmail:" + token, user.getEmail(), java.time.Duration.ofHours(24));
        emailService.sendVerificationEmail(user.getEmail(), token);

        return new AuthResponse(null, user.getUsername(), "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.");
    }

    @Override
    public java.util.Map<String, Object> login(LoginRequest request) {
        log.info("Received login request - Username: '{}'", request.username());
        
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        otpService.sendOtp(user.getEmail());

        String tempToken = java.util.UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("tempToken:" + tempToken, user.getEmail(), java.time.Duration.ofMinutes(5));

        return java.util.Map.of(
            "status", "REQUIRE_OTP",
            "tempToken", tempToken
        );
    }

    @Override
    public AuthResponse loginManager(LoginRequest request) {
        log.info("Received manager login request - EmployeeCode: '{}'", request.username());
        
        com.tony.demo.modules.user.domain.Staff staff = staffRepository.findByEmployeeCode(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), staff.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MANAGER"));
        
        if (staff.getRole() != null && staff.getRole().getPermissions() != null) {
            staff.getRole().getPermissions().forEach(p -> 
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(p.getCode()))
            );
        }

        org.springframework.security.core.userdetails.UserDetails userDetails = 
                org.springframework.security.core.userdetails.User.withUsername(staff.getEmployeeCode())
                .password(staff.getPassword())
                .authorities(authorities)
                .build();

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        jakarta.servlet.http.HttpServletRequest httpRequest = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();
        jakarta.servlet.http.HttpSession session = httpRequest.getSession(true);
        session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, org.springframework.security.core.context.SecurityContextHolder.getContext());

        String token = session.getId();

        return new AuthResponse(token, staff.getEmployeeCode(), "Manager login successful");
    }

    @Override
    public AuthResponse verifyOtp(com.tony.demo.modules.auth.dto.VerifyOtpRequest request) {
        String email = redisTemplate.opsForValue().get("tempToken:" + request.getTempToken());
        if (email == null) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        boolean isValid = otpService.verifyOtp(email, request.getOtp());
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        jakarta.servlet.http.HttpServletRequest httpRequest = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();
        jakarta.servlet.http.HttpSession session = httpRequest.getSession(true);
        session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, org.springframework.security.core.context.SecurityContextHolder.getContext());

        String token = session.getId(); // Return the session ID optionally

        redisTemplate.delete("tempToken:" + request.getTempToken());

        return new AuthResponse(token, user.getUsername(), "Login successful");
    }

    @Override
    public AuthResponse verifyEmailToken(String token) {
        String email = redisTemplate.opsForValue().get("verifyEmail:" + token);
        if (email == null) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS); // or a new ErrorCode.INVALID_TOKEN
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Mark user email as verified (Assume there's an isEmailVerified field, if not just log it for now)
        // If User doesn't have isEmailVerified, we can just delete the token to signify completion
        redisTemplate.delete("verifyEmail:" + token);
        log.info("Email {} has been verified successfully.", email);
        
        org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        jakarta.servlet.http.HttpServletRequest httpRequest = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();
        jakarta.servlet.http.HttpSession session = httpRequest.getSession(true);
        session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, org.springframework.security.core.context.SecurityContextHolder.getContext());

        String authToken = session.getId();
        return new AuthResponse(authToken, user.getUsername(), "Xác thực email thành công.");
    }
}
