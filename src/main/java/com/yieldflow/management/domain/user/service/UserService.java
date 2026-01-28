package com.yieldflow.management.domain.user.service;

import org.springframework.stereotype.Service;

import com.yieldflow.management.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

}
