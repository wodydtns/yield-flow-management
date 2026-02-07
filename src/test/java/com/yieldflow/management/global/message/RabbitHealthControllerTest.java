package com.yieldflow.management.global.message;

import com.yieldflow.management.global.response.ApiResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RabbitHealthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = FeignAutoConfiguration.class)
@TestPropertySource(properties = "bithumb.api-url=http://localhost")
class RabbitHealthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private RabbitHealthService rabbitHealthService;

        @Test
        @DisplayName("GET /api/health/rabbitmq - RabbitMQ 연결 성공")
        void checkRabbitmqConnection_success() throws Exception {
                // Given
                Map<String, Object> data = Map.of(
                                "status", "connected",
                                "connectionOpen", true,
                                "queuesDeclared", true,
                                "testMessageSent", true,
                                "message", "RabbitMQ is ready for FastAPI communication");
                var responseBody = ApiResponse.<Map<String, Object>>builder().data(data).build();
                given(rabbitHealthService.checkConnection()).willReturn(ResponseEntity.ok(responseBody));

                // When & Then
                mockMvc.perform(get("/api/health/rabbitmq"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("connected"))
                                .andExpect(jsonPath("$.data.connectionOpen").value(true))
                                .andExpect(jsonPath("$.data.queuesDeclared").value(true))
                                .andExpect(jsonPath("$.data.testMessageSent").value(true));
        }

        @Test
        @DisplayName("GET /api/health/rabbitmq - RabbitMQ 연결 실패")
        void checkRabbitmqConnection_failure() throws Exception {
                // Given
                var responseBody = ApiResponse.<Map<String, Object>>builder()
                                .error(ApiResponse.Error.of("RABBITMQ_CONNECTION_FAILED",
                                                "RabbitMQ connection is closed"))
                                .build();
                given(rabbitHealthService.checkConnection())
                                .willReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseBody));

                // When & Then
                mockMvc.perform(get("/api/health/rabbitmq"))
                                .andExpect(status().isServiceUnavailable())
                                .andExpect(jsonPath("$.error.errorCode").value("RABBITMQ_CONNECTION_FAILED"))
                                .andExpect(jsonPath("$.error.errorMessage").value("RabbitMQ connection is closed"));
        }
}
