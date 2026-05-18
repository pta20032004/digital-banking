package com.tony.demo.modules.account.application;

import com.tony.demo.modules.account.api.dto.response.AccountDetailResponse;
import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.user.domain.UserRepository;
import com.tony.demo.core.exception.AppException;
import com.tony.demo.core.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public List<AccountDetailResponse> getAccountsByUserPublicId(UUID userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        List<Account> accounts = accountRepository.findByUserId(user.getId());
        return accounts.stream()
                .map(account -> new AccountDetailResponse(
                        account.getPublicId(),
                        account.getAccountNumber(),
                        account.getBalance(),
                        account.getStatus().name(),
                        account.getCurrency()
                ))
                .collect(Collectors.toList());
    }
}
