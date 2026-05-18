// package com.tony.demo.infra.config;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.factory.PasswordEncoderFactories;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.provisioning.InMemoryUserDetailsManager;

// @Configuration
// public class SystemUserConfig {
//     @Value("${system.user.name}")
//     private String admin;

//     @Value("${system.user.password}")
//     private String password;

//     @Bean
//     public UserDetailsService userDetailsService() {
//         var userAdmin = User.withUsername(this.admin)
//                 .password(this.password)
//                 .roles("ADMIN")
//                 .build();
//         return new InMemoryUserDetailsManager(userAdmin);
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         // Giữ cho là bộ mã hóa không làm gì cả với prefix {noop}
//         return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//     }
// }
