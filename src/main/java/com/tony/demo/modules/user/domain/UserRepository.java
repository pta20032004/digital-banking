package com.tony.demo.modules.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
//Đặc trưng của bảng là read rất nhiều và write ít, dùng RC
public interface UserRepository extends JpaRepository<User, Long> {
    // Vì mặc định RC nên không cần jsql gì
    Optional<User> findByPublicId(UUID publicId);


    // for Spring Security / JWT
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Khi check trùng lặp data (đăng ký tài khoản), luôn dùng hàm existsBy... 
    // Nó sinh ra câu lệnh: SELECT 1 FROM users WHERE ... LIMIT 1
    // Nhanh hơn rất nhiều so với dùng findBy... rồi check null.
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByIdentityNumber(String identityNumber);
    
    java.util.List<User> findAllByKycStatus(UserStatus kycStatus);
    
    long countByKycStatus(UserStatus kycStatus);
}