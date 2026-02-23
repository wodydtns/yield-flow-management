package com.yieldflow.management.global.external.bithumb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BithumbAccountResponseDto(
        String currency,
        String balance,
        String locked,
        @JsonProperty("avg_buy_price") String avgBuyPrice,
        @JsonProperty("avg_buy_price_modified") String avgBuyPriceModified,
        @JsonProperty("unit_currency") String unitCurrency) {
}
