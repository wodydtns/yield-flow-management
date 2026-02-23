package com.yieldflow.management.global.external.binance.dto;

import java.util.List;

public record BinanceExchangeInfoResponseDto(String timezone, Long serverTime, List<BinanceSymbolDto> symbols) {
}
