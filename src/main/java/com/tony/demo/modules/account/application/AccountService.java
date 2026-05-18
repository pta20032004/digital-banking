package com.tony.demo.modules.account.application;

import com.tony.demo.modules.account.api.dto.response.AccountDetailResponse;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<AccountDetailResponse> getAccountsByUserPublicId(UUID userPublicId);
}
