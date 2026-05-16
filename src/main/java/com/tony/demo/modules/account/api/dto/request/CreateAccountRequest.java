// src/main/java/com/tony/demo/modules/account/api/dto/request/CreateAccountRequest.java
package com.tony.demo.modules.account.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAccountRequest(
        @NotNull(message = "ID người dùng không được để trống")
        UUID userPublicId,

        @NotBlank(message = "Loại tiền tệ không được để trống")
        String currency
) {
}
