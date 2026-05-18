package com.tony.demo.modules.account.api;

import com.tony.demo.modules.account.api.dto.response.AccountDetailResponse;
import com.tony.demo.modules.account.application.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;



    @GetMapping("/me/balance")
    public ResponseEntity<List<AccountDetailResponse>> getMyBalance() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(accountService.getAccountsByUsername(username));
    }
}
