package com.tony.demo.modules.user.api.dto.response;

import java.util.Set;

public record RoleDTO(
    String name,
    String code,
    Set<PermissionDTO> permissions
) {}
