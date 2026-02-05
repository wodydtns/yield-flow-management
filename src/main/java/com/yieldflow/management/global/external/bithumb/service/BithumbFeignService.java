package com.yieldflow.management.global.external.bithumb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yieldflow.management.domain.order.dto.OrderChanceResponseDto;
import com.yieldflow.management.global.external.bithumb.client.BithumbFeignClient;
import com.yieldflow.management.global.external.bithumb.dto.BithumbAccountResponseDto;
import com.yieldflow.management.global.external.bithumb.dto.BithumbMarketCodeResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BithumbFeignService {

    private final BithumbFeignClient bithumbFeignClient;

    /**
     * 계좌 조회 (GET /v1/accounts)
     */
    public List<BithumbAccountResponseDto> getAccounts() {
        log.info("Fetching Bithumb accounts using Feign");
        List<BithumbAccountResponseDto> accounts = bithumbFeignClient.getAccounts();
        log.info("Fetched {} accounts", accounts.size());
        return accounts;
    }

    /**
     * 주문 가능 정보 조회 (GET /v1/orders/chance)
     * 
     * @param market 마켓 코드 (예: KRW-BTC)
     */
    public OrderChanceResponseDto getOrderChance(String market) {
        log.info("Fetching order chance for market: {}", market);
        OrderChanceResponseDto orderChance = bithumbFeignClient.getOrderChance(market);
        log.info("Order chance fetched successfully for market: {}", market);
        return orderChance;
    }

    public List<BithumbMarketCodeResponseDto> getMarketCodes() {
        log.info("Fetching market codes using Feign");
        List<BithumbMarketCodeResponseDto> marketCodes = bithumbFeignClient.getMarketCodes();
        log.info("Fetched {} market codes", marketCodes.size());
        return marketCodes;
    }
}
