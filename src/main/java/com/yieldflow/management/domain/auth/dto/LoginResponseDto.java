package com.yieldflow.management.domain.auth.dto;

import com.yieldflow.management.global.enums.UserRole;
import com.yieldflow.management.global.enums.UserStatus;

public record LoginResponseDto(
        Long userId,
        String email,
        String nickname,
        UserRole role,
        UserStatus status) {
}
