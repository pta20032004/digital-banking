package com.tony.demo.modules.account.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) //khóa bi quan for update
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") // Tránh treo DB quá 3s
    })
    @Query("SELECT a FROM Account a WHERE a.id IN :ids ORDER BY a.id ASC") //jsql
    List<Account> findByIdsForUpdate(@Param("ids") List<Long> ids);

    // Backup Native Query 
    @Query(value = "SELECT * FROM accounts WHERE id IN :ids ORDER BY id ASC FOR UPDATE", nativeQuery = true)
    List<Account> findByIdsForUpdateNative(@Param("ids") List<Long> ids);

    // nạp/rút 
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    // Query thường
    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByPublicId(UUID publicId);

    List<Account> findByUserId(Long userId);
    
    // 4. KIỂM TRA TỒN TẠI TỐI ƯU HIỆU NĂNG
    boolean existsByAccountNumber(String accountNumber);

}
