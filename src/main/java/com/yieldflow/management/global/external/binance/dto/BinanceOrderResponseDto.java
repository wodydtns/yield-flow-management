package com.yieldflow.management.global.external.binance.dto;

import java.math.BigDecimal;

/**
 * Binance order response DTO (market orders)
 */
public record BinanceOrderResponseDto(
        Long orderId,
        String symbol,
        String clientOrderId,
        Long transactTime,
        BigDecimal price,
        BigDecimal origQty,
        BigDecimal executedQty,
        BigDecimal cummulativeQuoteQty,
        String status,
        String timeInForce,
        String type,
        String side) {
}
