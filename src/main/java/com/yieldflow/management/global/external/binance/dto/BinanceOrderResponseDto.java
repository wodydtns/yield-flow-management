package com.yieldflow.management.global.external.binance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BinanceOrderResponseDto(
                long orderId,
                String symbol,
                @JsonProperty("clientOrderId") String clientOrderId,
                long transactTime,
                String price,
                String origQty,
                String executedQty,
                String cummulativeQuoteQty,
                String status,
                String timeInForce,
                String type,
                String side) {
}
