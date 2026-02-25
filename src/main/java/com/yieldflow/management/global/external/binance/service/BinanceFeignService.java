package com.yieldflow.management.global.external.binance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yieldflow.management.global.external.binance.client.BinanceFeignClient;
import com.yieldflow.management.global.external.binance.dto.BinanceExchangeInfoResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceOrderResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceServerTimeDto;
import com.yieldflow.management.global.external.binance.dto.BinanceTickerPriceDto;
import com.yieldflow.management.global.external.binance.dto.account.BinanceAccountInfoDto;

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

    public BinanceAccountInfoDto getAccountInfo(String apiKey, long timestamp, String signature) {
        log.info("Fetching Binance account info via Feign");
        return binanceFeignClient.getAccountInfo(apiKey, timestamp, signature);
    }

    public BinanceOrderResponseDto placeLimitOrder(String apiKey, String symbol, String side, String timeInForce,
            String quantity, String price, long timestamp, String signature) {
        log.info("Placing Binance limit order via Feign for symbol {} side {} qty {} price {}", symbol, side, quantity,
                price);
        return binanceFeignClient.placeOrder(apiKey, symbol, side, "LIMIT", timeInForce, quantity, price, timestamp,
                signature);
    }
}
