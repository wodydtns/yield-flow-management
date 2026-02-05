package com.yieldflow.management.global.external.bithumb.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yieldflow.management.domain.order.dto.OrderChanceResponseDto;
import com.yieldflow.management.global.external.bithumb.dto.BithumbAccountResponseDto;
import com.yieldflow.management.global.external.bithumb.dto.BithumbMarketCodeResponseDto;

@FeignClient(name = "bithumbClient", url = "${bithumb.api-url}", configuration = com.yieldflow.management.global.config.FeignConfig.class)
public interface BithumbFeignClient {

    /**
     * 계좌 조회 (GET /v1/accounts)
     */
    @GetMapping("/v1/accounts")
    List<BithumbAccountResponseDto> getAccounts();

    /**
     * 주문 가능 정보 조회 (GET /v1/orders/chance)
     * 
     * @param market 마켓 코드 (예: KRW-BTC)
     */
    @GetMapping("/v1/orders/chance")
    OrderChanceResponseDto getOrderChance(@RequestParam("market") String market);

    @GetMapping("/v1/market/all")
    List<BithumbMarketCodeResponseDto> getMarketCodes();
}
