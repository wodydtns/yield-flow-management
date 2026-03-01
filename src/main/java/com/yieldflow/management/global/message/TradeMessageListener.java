package com.yieldflow.management.global.message;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.yieldflow.management.global.config.RabbitConfig;
import com.yieldflow.management.global.external.binance.service.BinanceFeignService;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * RabbitMQ Message Listener for FastAPI communication
 * FastAPI에서 보낸 메시지를 수신하는 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeMessageListener {

    private final BinanceFeignService binanceFeignService;

    /**
     * 주문 큐 메시지 수신
     */
    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void receiveOrderMessage(OrderMessage message) {
        log.info("[RabbitMQ][ORDER] Handler invoked. payload={}", message);

        var side = message.side();
        if (side == null) {
            log.warn("[RabbitMQ][ORDER] side is null. messageId={} market={} volume={} price={}",
                    message.orderId(), message.market(), message.volume(), message.price());
            return;
        }

        switch (side.toUpperCase()) {
            case "BUY" -> {
                log.info("[RabbitMQ][ORDER] Executing BUY: market={} volume={} price={}",
                        message.market(), message.volume(), message.price());
                binanceFeignService.placeLimitOrderSigned(message.market(), "BUY", "GTC",
                        String.valueOf(message.volume()), String.valueOf(message.price()));
            }
            case "SELL" -> {
                log.info("[RabbitMQ][ORDER] Executing SELL: market={} volume={} price={}",
                        message.market(), message.volume(), message.price());
                binanceFeignService.placeLimitOrderSigned(message.market(), "SELL", "GTC",
                        String.valueOf(message.volume()), String.valueOf(message.price()));
            }
            default -> log.warn("[RabbitMQ][ORDER] Unknown side received: {}", side);
        }
    }

    /**
     * 거래 큐 메시지 수신
     */
    @RabbitListener(queues = RabbitConfig.TRADE_QUEUE)
    public void receiveTradeMessage(TradeMessage message) {
        log.info("[RabbitMQ][TRADE] Handler invoked. payload={}", message);
        log.info("Received trade message from FastAPI: {}", message);
        // TODO: Process trade message from FastAPI
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
            LocalDateTime timestamp) {
    }

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
            LocalDateTime timestamp) {
    }

}
