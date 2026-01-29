package com.yieldflow.management.auth.service;

import org.springframework.stereotype.Service;

import com.yieldflow.management.auth.dto.LoginRequestDto;
import com.yieldflow.management.auth.repository.AuthRepository;
import com.yieldflow.management.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;

    public User login(LoginRequestDto loginRequestDto) {
        return authRepository.login(loginRequestDto);
    }
}
