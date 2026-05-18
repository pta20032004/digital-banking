package com.tony.demo.modules.user.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingUsersResponse {
    private long totalPending;
    private List<PendingUserDto> users;
}
