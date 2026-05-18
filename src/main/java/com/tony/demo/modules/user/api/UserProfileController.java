package com.tony.demo.modules.user.api;

import com.tony.demo.core.enums.ErrorCode;
import com.tony.demo.core.exception.AppException;
import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.user.domain.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        List<Account> accounts = accountRepository.findByUserId(user.getId());
        String accountNumber = accounts.isEmpty() ? null : accounts.get(0).getAccountNumber();

        return ResponseEntity.ok(new UserProfileResponse(user.getFullName(), accountNumber));
    }

    @Data
    @AllArgsConstructor
    public static class UserProfileResponse {
        private String fullName;
        private String accountNumber;
    }
}
