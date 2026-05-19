package com.tony.demo.modules.manager.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.tony.demo.modules.manager.application.ManagerService;
import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.transaction.domain.Transaction;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    // 1. Duyệt KYC (Mã 2001)
    @PutMapping("/users/{accountNumber}/kyc-approve")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('KYC_APPROVE')")
    public ResponseEntity<Void> approveKyc(@PathVariable String accountNumber) {
        managerService.approveKyc(accountNumber);
        return ResponseEntity.ok().build();
    }

    // 2. Khóa tài khoản người dùng (Mã 2002)
    @PutMapping("/users/{accountNumber}/lock")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('USER_LOCK')")
    public ResponseEntity<Void> lockUser(@PathVariable String accountNumber) {
        managerService.lockUser(accountNumber);
        return ResponseEntity.ok().build();
    }

    // 3. Tìm kiếm user theo account number
    @GetMapping("/users/{accountNumber}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('USER_VIEW')")
    public ResponseEntity<User> getUserByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(managerService.getUserByAccountNumber(accountNumber));
    }

    // 4. Xem toàn bộ giao dịch trên hệ thống (Mã 2003) (có phân trang)
    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('TRANS_VIEW_ALL')")
    public ResponseEntity<Page<Transaction>> getAllTransactions(Pageable pageable) {
        return ResponseEntity.ok(managerService.getAllTransactions(pageable));
    }

    // 5. Xem danh sách user đang chờ KYC
    @GetMapping("/users/pending-kyc")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('KYC_APPROVE')")
    public ResponseEntity<java.util.List<com.tony.demo.modules.manager.api.dto.PendingKycUserResponse>> getPendingKycUsers() {
        return ResponseEntity.ok(managerService.getPendingKycUsers());
    }
}
