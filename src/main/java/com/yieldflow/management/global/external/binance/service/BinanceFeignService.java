package com.yieldflow.management.global.external.binance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yieldflow.management.global.external.binance.client.BinanceFeignClient;
import com.yieldflow.management.global.external.binance.dto.BinanceExchangeInfoResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceServerTimeDto;
import com.yieldflow.management.global.external.binance.dto.BinanceTickerPriceDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceFeignService {

    private final BinanceFeignClient binanceFeignClient;

    public BinanceServerTimeDto getServerTime() {
        log.info("Fetching Binance server time via Feign");
        return binanceFeignClient.getServerTime();
    }

    public List<BinanceTickerPriceDto> getTickerPrices() {
        log.info("Fetching Binance ticker prices via Feign");
        var prices = binanceFeignClient.getTickerPrices();
        log.info("Fetched {} Binance ticker prices", prices.size());
        return prices;
    }

    public BinanceExchangeInfoResponseDto getExchangeInfo() {
        log.info("Fetching Binance exchange info via Feign");
        return binanceFeignClient.getExchangeInfo();
    }
}
