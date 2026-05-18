package com.tony.demo.modules.transaction.application;

import com.tony.demo.modules.transaction.api.dto.request.TransferRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tony.demo.modules.transaction.api.dto.response.TransactionHistoryResponse;

public interface TransactionService {
    void executeTransfer(TransferRequest request);
    Page<TransactionHistoryResponse> getTransactionHistory(String username, Pageable pageable);
}
