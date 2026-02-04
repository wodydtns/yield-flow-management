package com.yieldflow.management.domain.auth.service;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yieldflow.management.domain.auth.dto.LoginRequestDto;
import com.yieldflow.management.domain.auth.dto.LoginResponseDto;
import com.yieldflow.management.domain.auth.repository.AuthRepository;
import com.yieldflow.management.domain.user.entity.User;
import com.yieldflow.management.global.exception.DomainException;
import com.yieldflow.management.global.exception.DomainExceptionCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletRequest request,
            HttpServletResponse response) {
        User user = authRepository.findByEmail(loginRequestDto.email());

        if (user == null) {
            throw new DomainException(DomainExceptionCode.INVALID_EMAIL_OR_PASSWORD);
        }

        boolean matches = passwordEncoder.matches(loginRequestDto.password(), user.getPassword());

        if (!matches) {
            throw new DomainException(DomainExceptionCode.INVALID_EMAIL_OR_PASSWORD);
        }

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        var authentication = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("role", user.getRole().name());

        session.setMaxInactiveInterval(30 * 60);

        log.info("User logged in successfully: {}, role: {}", user.getEmail(), user.getRole());

        return new LoginResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus());
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
