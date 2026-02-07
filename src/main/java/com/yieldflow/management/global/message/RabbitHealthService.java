package com.yieldflow.management.global.message;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.yieldflow.management.global.config.RabbitConfig;
import com.yieldflow.management.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitHealthService {

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;

    /**
     * RabbitMQ 연결 상태 및 FastAPI 통신 준비 상태 확인
     */
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkConnection() {
        try {
            // 1. 연결 상태 확인
            var connection = connectionFactory.createConnection();
            boolean isOpen = connection.isOpen();
            connection.close();

            if (!isOpen) {
                return ApiResponse.fail(HttpStatus.SERVICE_UNAVAILABLE, "RABBITMQ_CONNECTION_FAILED",
                        "RabbitMQ connection is closed");
            }

            // 2. 큐 존재 여부 확인 및 생성
            RabbitAdmin admin = new RabbitAdmin(connectionFactory);
            Queue orderQueue = new Queue(RabbitConfig.ORDER_QUEUE, true);
            Queue tradeQueue = new Queue(RabbitConfig.TRADE_QUEUE, true);
            Queue notificationQueue = new Queue(RabbitConfig.NOTIFICATION_QUEUE, true);

            admin.declareQueue(orderQueue);
            admin.declareQueue(tradeQueue);
            admin.declareQueue(notificationQueue);

            // 3. 테스트 메시지 발행 (FastAPI와 통신 테스트용)
            var testMessage = new ConnectionTestMessage(
                    "test",
                    "RabbitMQ connection test from Spring Boot",
                    LocalDateTime.now());

            rabbitTemplate.convertAndSend(RabbitConfig.ORDER_QUEUE, testMessage);
            log.info("Test message sent to queue: {}", RabbitConfig.ORDER_QUEUE);

            return ResponseEntity.ok(ApiResponse.ok(Map.of(
                    "status", "connected",
                    "connectionOpen", true,
                    "queuesDeclared", true,
                    "testMessageSent", true,
                    "timestamp", LocalDateTime.now().toString(),
                    "message", "RabbitMQ is ready for FastAPI communication")));

        } catch (AmqpException e) {
            log.error("RabbitMQ connection check failed", e);
            return ApiResponse.fail(HttpStatus.SERVICE_UNAVAILABLE, "RABBITMQ_CONNECTION_FAILED", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during RabbitMQ connection check", e);
            return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "RABBITMQ_CHECK_ERROR", e.getMessage());
        }
    }

    /**
     * 연결 테스트용 메시지 레코드
     */
    public record ConnectionTestMessage(
            String type,
            String content,
            LocalDateTime timestamp) {
    }
}
