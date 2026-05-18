package com.tony.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.tony.demo.config.filter.CustomUsernamePasswordAuthenticationFilter;
import com.tony.demo.config.filter.CustomOtpAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1. ChannelProcessingFilter (Bảo vệ tầng vận chuyển)
        // Tạm thời vô hiệu hóa theo yêu cầu
        /*
        http.requiresChannel(channel -> channel
            .anyRequest().requiresSecure()
        );
        */

        // 2. SecurityContextHolderFilter (Quản lý phiên) & 8. ConcurrentSessionFilter
        http.sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1) // 8. ConcurrentSessionFilter: Kiểm soát đa thiết bị
            .maxSessionsPreventsLogin(false)
        );

        // 3. HeaderWriterFilter (Bảo vệ trình duyệt)
        http.headers(headers -> headers
            .frameOptions(frame -> frame.deny()) // X-Frame-Options: DENY
            .contentTypeOptions(Customizer.withDefaults()) // X-Content-Type-Options: nosniff
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")) // CSP
        );

        // 4. CorsFilter (Kiểm soát nguồn gốc)
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 5. CsrfFilter (Chống lệnh lậu)
        http.csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers("/api/v1/auth/**", "/login", "/verify-otp")
        );

        // 9. ExceptionTranslationFilter (Xử lý lỗi bảo mật)
        http.exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Vui lòng đăng nhập\"}");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Không có quyền truy cập\"}");
            })
        );

        // 10. AuthorizationFilter (Cổng cuối - Phân quyền)
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**", "/api/v1/auth/**", "/login", "/verify-otp").permitAll()
            .anyRequest().authenticated()
        );

        // 6 & 7. Custom Filters (Logic đăng nhập và OTP)
        // Bỏ custom filter do đã chuyển xử lý vào AuthController và AuthServiceImpl
        // http.addFilterAt(new CustomUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        // http.addFilterAfter(new CustomOtpAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://ebank.com", "http://localhost:5173", "http://localhost:3000", "http://localhost:5000", "http://127.0.0.1:5500")); // Whitelist domain
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
