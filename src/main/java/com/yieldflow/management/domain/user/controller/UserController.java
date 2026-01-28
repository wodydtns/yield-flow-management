package com.yieldflow.management.domain.user.controller;

import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

}
