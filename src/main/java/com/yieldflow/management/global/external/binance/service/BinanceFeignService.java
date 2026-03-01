package com.yieldflow.management.global.external.binance.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yieldflow.management.global.external.binance.client.BinanceFeignClient;
import com.yieldflow.management.global.external.binance.dto.BinanceExchangeInfoResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceOrderResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceServerTimeDto;
import com.yieldflow.management.global.external.binance.dto.BinanceTickerPriceDto;
import com.yieldflow.management.global.external.binance.dto.account.BinanceAccountInfoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceFeignService {

    private final BinanceFeignClient binanceFeignClient;

    @Value("${binance.api-key}")
    private String apiKey;

    @Value("${binance.api-secret}")
    private String apiSecret;

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

    public BinanceOrderResponseDto placeLimitOrderSigned(String symbol, String side, String timeInForce,
            String quantity,
            String price) {
        try {
            long timestamp = Instant.now().toEpochMilli();
            var query = String.format(
                    "symbol=%s&side=%s&type=LIMIT&timeInForce=%s&quantity=%s&price=%s&timestamp=%d",
                    symbol, side, timeInForce, quantity, price, timestamp);
            var signature = sign(query, apiSecret);

            log.info("Placing Binance limit order (signed) symbol={} side={} qty={} price={}", symbol, side, quantity,
                    price);
            return placeLimitOrder(apiKey, symbol, side, timeInForce, quantity, price, timestamp, signature);
        } catch (Exception e) {
            log.error("Failed to place signed Binance order", e);
            throw new RuntimeException("Failed to place signed Binance order", e);
        }
    }

    private String sign(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
