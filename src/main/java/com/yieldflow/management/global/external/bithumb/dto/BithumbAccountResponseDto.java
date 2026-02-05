package com.yieldflow.management.global.external.bithumb.dto;

public record BithumbAccountResponseDto(
                String currency,
                String balance,
                String locked,
                String avg_buy_price,
                String avg_buy_price_modified,
                String unit_currency) {
}
