package com.yieldflow.management.domain.user.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.yieldflow.management.domain.user.dto.UserRequestDto;
import com.yieldflow.management.domain.user.service.UserService;
import com.yieldflow.management.global.external.bithumb.dto.BithumbAccountResponseDto;
import com.yieldflow.management.global.external.bithumb.service.BithumbService;
import com.yieldflow.management.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BithumbService bithumbService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        userService.createUser(userRequestDto);
        return ApiResponse.ok();
    }

    @GetMapping("/bithumb-account")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BithumbAccountResponseDto>> getUserBithumbAccount() {
        return ApiResponse.ok(bithumbService.getAccounts());
    }
}
