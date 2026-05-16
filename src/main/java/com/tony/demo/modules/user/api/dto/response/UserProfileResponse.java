// src/main/java/com/tony/demo/modules/user/api/dto/response/UserProfileResponse.java
package com.tony.demo.modules.user.api.dto.response;

import java.util.UUID;

public record UserProfileResponse(
        UUID publicId,
        String fullName,
        String email,
        String phoneNumber,
        String kycStatus
) {
}
