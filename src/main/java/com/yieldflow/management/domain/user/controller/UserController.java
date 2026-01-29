package com.yieldflow.management.domain.user.controller;

import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.domain.user.dto.UserRequestDto;
import com.yieldflow.management.domain.user.service.UserService;
import com.yieldflow.management.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createUser(@RequestBody UserRequestDto userRequestDto) {
        userService.createUser(userRequestDto);
        return ApiResponse.ok();
    }
}
