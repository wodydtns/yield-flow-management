package com.yieldflow.management.global.external.binance.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.yieldflow.management.global.external.binance.dto.BinanceExchangeInfoResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceOrderResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceServerTimeDto;
import com.yieldflow.management.global.external.binance.dto.BinanceTickerPriceDto;
import com.yieldflow.management.global.external.binance.dto.account.BinanceAccountInfoDto;

@FeignClient(name = "binanceClient", url = "${binance.api-url}", configuration = com.yieldflow.management.global.config.BinanceFeignConfig.class)
public interface BinanceFeignClient {

        @GetMapping("/api/v3/time")
        BinanceServerTimeDto getServerTime();

        @GetMapping("/api/v3/ticker/price")
        List<BinanceTickerPriceDto> getTickerPrices();

        @GetMapping("/api/v3/exchangeInfo")
        BinanceExchangeInfoResponseDto getExchangeInfo();

        @GetMapping("/api/v3/account")
        BinanceAccountInfoDto getAccountInfo(
                        @RequestHeader("X-MBX-APIKEY") String apiKey,
                        @RequestParam("timestamp") long timestamp,
                        @RequestParam("signature") String signature);

        @PostMapping("/api/v3/order")
        BinanceOrderResponseDto placeOrder(
                        @RequestHeader("X-MBX-APIKEY") String apiKey,
                        @RequestParam("symbol") String symbol,
                        @RequestParam("side") String side,
                        @RequestParam("type") String type,
                        @RequestParam("timeInForce") String timeInForce,
                        @RequestParam("quantity") String quantity,
                        @RequestParam("price") String price,
                        @RequestParam("timestamp") long timestamp,
                        @RequestParam("signature") String signature);

}
