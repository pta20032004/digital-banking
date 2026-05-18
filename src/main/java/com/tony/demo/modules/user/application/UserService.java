package com.tony.demo.modules.user.application;

import com.tony.demo.modules.user.dto.PendingUsersResponse;

public interface UserService {
    PendingUsersResponse getAllPendingUsers();
    byte[] getKycDocumentImage(String fileName);
}
