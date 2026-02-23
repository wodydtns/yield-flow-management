package com.yieldflow.management.global.external.binance.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.yieldflow.management.global.external.binance.dto.BinanceExchangeInfoResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceServerTimeDto;
import com.yieldflow.management.global.external.binance.dto.BinanceTickerPriceDto;

@FeignClient(name = "binanceClient", url = "${binance.api-url}", configuration = com.yieldflow.management.global.config.BinanceFeignConfig.class)
public interface BinanceFeignClient {

    @GetMapping("/api/v3/time")
    BinanceServerTimeDto getServerTime();

    @GetMapping("/api/v3/ticker/price")
    List<BinanceTickerPriceDto> getTickerPrices();

    @GetMapping("/api/v3/exchangeInfo")
    BinanceExchangeInfoResponseDto getExchangeInfo();
}
