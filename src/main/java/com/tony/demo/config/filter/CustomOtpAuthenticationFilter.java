package com.tony.demo.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class CustomOtpAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Bước 2: Bắt request xác thực OTP
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/verify-otp".equals(request.getRequestURI())) {
            
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            
            // Kiểm tra xem user có đang ở trạng thái chờ OTP không (ROLE_PRE_AUTH)
            boolean isPreAuth = currentAuth != null && currentAuth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PRE_AUTH"));

            if (isPreAuth) {
                // TODO: Lấy OTP từ request body và so sánh với DB/Redis
                boolean isOtpValid = true; // Mặc định pass để minh hoạ

                if (isOtpValid) {
                    // Xóa ROLE_PRE_AUTH, nạp toàn bộ Permissions thật
                    // TODO: Lấy quyền thật từ DB (bảng role_permissions)
                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            currentAuth.getPrincipal(), null, 
                            List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("MONEY_TRANSFER")));
                    
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\": \"Đăng nhập thành công\", \"status\": \"SUCCESS\"}");
                    return;
                } else {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Mã OTP không hợp lệ\"}");
                    return;
                }
            } else {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Phiên xác thực không hợp lệ hoặc đã hết hạn\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
