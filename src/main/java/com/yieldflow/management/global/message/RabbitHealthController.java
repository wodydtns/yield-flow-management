package com.yieldflow.management.global.message;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class RabbitHealthController {

    private final RabbitHealthService rabbitHealthService;

    /**
     * RabbitMQ 연결 상태 확인 엔드포인트
     * FastAPI에서 이 엔드포인트를 호출하여 Spring Boot와의 RabbitMQ 연결을 확인할 수 있습니다.
     */
    @GetMapping("/rabbitmq")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRabbitmqConnection() {
        return rabbitHealthService.checkConnection();
    }
}
