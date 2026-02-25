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

    public BinanceOrderResponseDto placeMarketBuyOrder(String symbol, String quantity, Long recvWindow) {
        return placeMarketOrder(symbol, quantity, "BUY", recvWindow);
    }

    public BinanceOrderResponseDto placeMarketSellOrder(String symbol, String quantity, Long recvWindow) {
        return placeMarketOrder(symbol, quantity, "SELL", recvWindow);
    }

    private BinanceOrderResponseDto placeMarketOrder(String symbol, String quantity, String side, Long recvWindow) {
        var timestamp = Instant.now().toEpochMilli();
        var signature = generateSignature(symbol, side, quantity, timestamp, recvWindow);
        log.info("Placing Binance {} market order for {} quantity {}", side, symbol, quantity);
        return binanceFeignClient.placeOrder(
                apiKey,
                symbol,
                side,
                "MARKET",
                quantity,
                timestamp,
                signature,
                recvWindow);
    }

    private String generateSignature(String symbol, String side, String quantity, long timestamp, Long recvWindow) {
        var base = new StringBuilder()
                .append("symbol=").append(symbol)
                .append("&side=").append(side)
                .append("&type=MARKET")
                .append("&quantity=").append(quantity)
                .append("&timestamp=").append(timestamp);

        if (recvWindow != null) {
            base.append("&recvWindow=").append(recvWindow);
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(base.toString().getBytes(StandardCharsets.UTF_8));
            var hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            log.error("Failed to generate Binance signature", e);
            throw new IllegalStateException("Failed to generate Binance signature", e);
        }
    }
}
