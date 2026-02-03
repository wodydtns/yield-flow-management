package com.yieldflow.management.domain.user.dto;

import com.yieldflow.management.domain.user.entity.User;
import com.yieldflow.management.global.enums.UserRole;
import com.yieldflow.management.global.enums.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 16, message = "비밀번호(password)는 8자리 이상 16자리 이하로 입력해야 합니다.") @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':,.<>/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':,.<>/?]{8,16}$", message = "비밀번호(password)는 대문자, 숫자, 특수문자를 포함해야 합니다.") String password,
        @NotBlank String nickname) {

    public User createUser(String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
