package com.yieldflow.management.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yieldflow.management.domain.user.dto.UserRequestDto;
import com.yieldflow.management.domain.user.service.UserService;
import com.yieldflow.management.global.external.bithumb.dto.BithumbAccountResponseDto;
import com.yieldflow.management.global.external.bithumb.service.BithumbFeignService;

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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = FeignAutoConfiguration.class)
@TestPropertySource(properties = "bithumb.api-url=http://localhost")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BithumbFeignService bithumbFeignService;

    @Test
    @DisplayName("POST /api/users - 회원가입 성공")
    void createUser_success() throws Exception {
        // Given
        var request = new UserRequestDto("test@example.com", "Password1!", "tester");
        willDoNothing().given(userService).createUser(any(UserRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/users - 이메일 형식 오류 시 400 반환")
    void createUser_invalidEmail_returns400() throws Exception {
        // Given
        var request = new UserRequestDto("invalid-email", "Password1!", "tester");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users - 비밀번호 누락 시 400 반환")
    void createUser_blankPassword_returns400() throws Exception {
        // Given
        var request = new UserRequestDto("test@example.com", "", "tester");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/bithumb-account - 빗썸 계좌 조회 성공")
    void getUserBithumbAccount_success() throws Exception {
        // Given
        var accounts = List.of(
                new BithumbAccountResponseDto("KRW", "1000000", "0", "0", "false", "KRW"),
                new BithumbAccountResponseDto("BTC", "0.5", "0", "50000000", "false", "KRW"));
        given(bithumbFeignService.getAccounts()).willReturn(accounts);

        // When & Then
        mockMvc.perform(get("/api/users/bithumb-account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].currency").value("KRW"))
                .andExpect(jsonPath("$.data[1].currency").value("BTC"));
    }
}
