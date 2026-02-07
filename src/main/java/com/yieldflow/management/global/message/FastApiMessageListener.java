package com.yieldflow.management.global.message;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.yieldflow.management.global.config.RabbitConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ Message Listener for FastAPI communication
 * FastAPI에서 보낸 메시지를 수신하는 리스너
 */
@Slf4j
@Component
public class FastApiMessageListener {

    /**
     * 주문 큐 메시지 수신
     */
    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void receiveOrderMessage(OrderMessage message) {
        log.info("Received order message from FastAPI: {}", message);
        // TODO: Process order message from FastAPI
    }

    /**
     * 거래 큐 메시지 수신
     */
    @RabbitListener(queues = RabbitConfig.TRADE_QUEUE)
    public void receiveTradeMessage(TradeMessage message) {
        log.info("Received trade message from FastAPI: {}", message);
        // TODO: Process trade message from FastAPI
    }

    /**
     * 알림 큐 메시지 수신
     */
    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void receiveNotificationMessage(NotificationMessage message) {
        log.info("Received notification message from FastAPI: {}", message);
        // TODO: Process notification message from FastAPI
    }

    /**
     * 주문 메시지 레코드
     */
    public record OrderMessage(
            String orderId,
            String market,
            String side,
            Double volume,
            Double price,
            String status,
            LocalDateTime timestamp
    ) {}

    /**
     * 거래 메시지 레코드
     */
    public record TradeMessage(
            String tradeId,
            String market,
            String side,
            Double volume,
            Double price,
            Double funds,
            LocalDateTime timestamp
    ) {}

    /**
     * 알림 메시지 레코드
     */
    public record NotificationMessage(
            String type,
            String title,
            String content,
            String userId,
            LocalDateTime timestamp
    ) {}
}
