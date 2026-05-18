package com.tony.demo.modules.transaction.api;

import com.tony.demo.modules.transaction.api.dto.request.TransferRequest;
import com.tony.demo.modules.transaction.application.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import com.tony.demo.modules.transaction.api.dto.response.TransactionHistoryResponse;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest request) {
        transactionService.executeTransfer(request);
        return ResponseEntity.ok("Giao dịch chuyển tiền thành công");
    }

    @GetMapping
    public ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(transactionService.getTransactionHistory(username, PageRequest.of(page, size)));
    }
}
