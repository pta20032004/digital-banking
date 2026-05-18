package com.tony.demo.modules.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByPublicId(UUID publicId);
    
    Optional<Staff> findByEmployeeCode(String employeeCode);
    
    Optional<Staff> findByEmail(String email);
    
    boolean existsByEmployeeCode(String employeeCode);
    
    boolean existsByEmail(String email);
}
