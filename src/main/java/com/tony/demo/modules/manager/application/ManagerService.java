package com.tony.demo.modules.manager.application;

import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.transaction.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.tony.demo.modules.manager.api.dto.PendingKycUserResponse;

public interface ManagerService {
    void approveKyc(String accountNumber);
    void lockUser(String accountNumber);
    User getUserByAccountNumber(String accountNumber);
    Page<Transaction> getAllTransactions(Pageable pageable);
    List<PendingKycUserResponse> getPendingKycUsers();
}
