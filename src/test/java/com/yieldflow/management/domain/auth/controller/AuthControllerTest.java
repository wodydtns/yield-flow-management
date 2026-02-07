package com.yieldflow.management.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yieldflow.management.domain.auth.dto.LoginRequestDto;
import com.yieldflow.management.domain.auth.dto.LoginResponseDto;
import com.yieldflow.management.domain.auth.service.AuthService;
import com.yieldflow.management.global.enums.UserRole;
import com.yieldflow.management.global.enums.UserStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = FeignAutoConfiguration.class)
@TestPropertySource(properties = "bithumb.api-url=http://localhost")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - 로그인 성공")
    void login_success() throws Exception {
        // Given
        var request = new LoginRequestDto("test@example.com", "password123");
        var response = new LoginResponseDto(1L, "test@example.com", "tester", UserRole.USER, UserStatus.ACTIVE);

        given(authService.login(any(LoginRequestDto.class), any(HttpServletRequest.class),
                any(HttpServletResponse.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - 로그아웃 성공")
    void logout_success() throws Exception {
        // Given
        willDoNothing().given(authService).logout(any(HttpServletRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Logout successful"));
    }
}
