package com.yieldflow.management.global.external.binance.dto;

import java.util.List;

public record BinanceSymbolDto(
        String symbol,
        String status,
        String baseAsset,
        String quoteAsset,
        List<String> permissions) {
}
