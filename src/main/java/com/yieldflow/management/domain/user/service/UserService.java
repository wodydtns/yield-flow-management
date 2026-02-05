package com.yieldflow.management.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yieldflow.management.domain.user.dto.UserRequestDto;
import com.yieldflow.management.domain.user.entity.User;
import com.yieldflow.management.domain.user.repository.UserRepository;
import com.yieldflow.management.global.exception.DomainException;
import com.yieldflow.management.global.exception.DomainExceptionCode;

import lombok.RequiredArgsConstructor;

@Service

@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public void createUser(UserRequestDto userRequestDto) {
        User user = userRepository.findByEmail(userRequestDto.email());
        String password = userRequestDto.password();
        String encodedPassword = passwordEncoder.encode(password);
        if (user != null) {
            throw new DomainException(DomainExceptionCode.USER_ALREADY_EXISTS);
        }
        userRepository.save(userRequestDto.createUser(encodedPassword));
    }

    public void updateVerified(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new DomainException(DomainExceptionCode.USER_NOT_FOUND);
        }
        user.setVerified();
        userRepository.save(user);

    }

}
