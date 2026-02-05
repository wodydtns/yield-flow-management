package com.yieldflow.management.domain.market.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.yieldflow.management.global.external.bithumb.dto.BithumbMarketCodeResponseDto;
import com.yieldflow.management.global.external.bithumb.service.BithumbFeignService;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final BithumbFeignService bithumbFeignService;

    private static final Set<String> TARGET_CURRENCIES = Set.of("BTC", "ETH", "USDT", "USDC");

    public List<BithumbMarketCodeResponseDto> getMarketCodes() {
        return bithumbFeignService.getMarketCodes().stream()
                .filter(market -> TARGET_CURRENCIES.stream()
                        .anyMatch(currency -> market.market().contains(currency)))
                .toList();
    }
}
