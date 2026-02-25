package com.yieldflow.management.domain.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.domain.order.dto.OrderChanceResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceExchangeInfoResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceOrderResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceServerTimeDto;
import com.yieldflow.management.global.external.binance.dto.BinanceTickerPriceDto;
import com.yieldflow.management.global.external.binance.service.BinanceFeignService;
import com.yieldflow.management.global.external.bithumb.service.BithumbFeignService;
import com.yieldflow.management.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final BithumbFeignService bithumbFeignService;
    private final BinanceFeignService binanceFeignService;

    @GetMapping("/chance")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderChanceResponseDto> getOrderChance(
            @RequestParam(defaultValue = "KRW-BTC") String market) {
        // return ApiResponse.ok(bithumbService.getOrderChance(market));
        return ApiResponse.ok(bithumbFeignService.getOrderChance(market));
    }

    @GetMapping("/binance/server-time")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BinanceServerTimeDto> getBinanceServerTime() {
        return ApiResponse.ok(binanceFeignService.getServerTime());
    }

    @GetMapping("/binance/ticker-prices")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BinanceTickerPriceDto>> getBinanceTickerPrices() {
        return ApiResponse.ok(binanceFeignService.getTickerPrices());
    }

    @GetMapping("/binance/exchange-info")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BinanceExchangeInfoResponseDto> getBinanceExchangeInfo() {
        return ApiResponse.ok(binanceFeignService.getExchangeInfo());
    }

    @PostMapping("/binance/order/buy")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BinanceOrderResponseDto> placeBinanceMarketBuy(
            @RequestParam String symbol,
            @RequestParam String quantity,
            @RequestParam(required = false) Long recvWindow) {
        return ApiResponse.ok(binanceFeignService.placeMarketBuyOrder(symbol, quantity, recvWindow));
    }

    @PostMapping("/binance/order/sell")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BinanceOrderResponseDto> placeBinanceMarketSell(
            @RequestParam String symbol,
            @RequestParam String quantity,
            @RequestParam(required = false) Long recvWindow) {
        return ApiResponse.ok(binanceFeignService.placeMarketSellOrder(symbol, quantity, recvWindow));
    }
}
