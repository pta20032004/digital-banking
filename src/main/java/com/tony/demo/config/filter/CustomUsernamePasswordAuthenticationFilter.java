package com.tony.demo.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class CustomUsernamePasswordAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Bước 1: Bắt request đăng nhập
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getRequestURI())) {
            org.springframework.web.context.WebApplicationContext context = 
                org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.tony.demo.modules.user.domain.UserRepository userRepository = context.getBean(com.tony.demo.modules.user.domain.UserRepository.class);
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class);
            com.tony.demo.modules.auth.application.OtpService otpService = context.getBean(com.tony.demo.modules.auth.application.OtpService.class);

            try {
                com.tony.demo.modules.auth.dto.LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), com.tony.demo.modules.auth.dto.LoginRequest.class);
                
                java.util.Optional<com.tony.demo.modules.user.domain.User> userOpt = userRepository.findByUsername(loginRequest.username());
                
                if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.password(), userOpt.get().getPassword())) {
                    com.tony.demo.modules.user.domain.User user = userOpt.get();
                    
                    // Cấp quyền tạm thời ROLE_PRE_AUTH
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user.getUsername(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_PRE_AUTH")));
                    
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // Sinh OTP và gửi qua Email
                    otpService.sendOtp(user.getEmail());
                    
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\": \"Đã gửi OTP qua email. Vui lòng xác thực.\", \"status\": \"PENDING_OTP\"}");
                    return;
                } else {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Sai tên đăng nhập hoặc mật khẩu\"}");
                    return;
                }
            } catch (Exception e) {
                response.setStatus(400);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Bad Request\", \"message\": \"Dữ liệu không hợp lệ\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
