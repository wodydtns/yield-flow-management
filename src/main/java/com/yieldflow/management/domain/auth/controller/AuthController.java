package com.yieldflow.management.domain.auth.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.domain.auth.dto.LoginRequestDto;
import com.yieldflow.management.domain.auth.dto.LoginResponseDto;
import com.yieldflow.management.domain.auth.service.AuthService;
import com.yieldflow.management.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ApiResponse.ok(authService.login(loginRequestDto, request, response));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ApiResponse.ok("Logout successful");
    }
}
