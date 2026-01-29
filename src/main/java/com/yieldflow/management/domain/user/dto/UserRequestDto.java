package com.yieldflow.management.domain.user.dto;

import com.yieldflow.management.domain.user.entity.User;
import com.yieldflow.management.global.enums.UserRole;
import com.yieldflow.management.global.enums.UserStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRequestDto {
    private String email;
    private String password;
    private String nickname;

    public User createUser(String password) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
