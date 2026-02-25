package com.yieldflow.management.domain.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.yieldflow.management.domain.order.dto.OrderChanceResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceExchangeInfoResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceOrderResponseDto;
import com.yieldflow.management.global.external.binance.dto.BinanceServerTimeDto;
import com.yieldflow.management.global.external.binance.dto.BinanceTickerPriceDto;
import com.yieldflow.management.global.external.binance.dto.account.BinanceAccountInfoDto;
import com.yieldflow.management.global.external.binance.service.BinanceFeignService;
import com.yieldflow.management.global.external.bithumb.service.BithumbFeignService;
import com.yieldflow.management.global.response.ApiResponse;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "주문 및 거래소 정보 조회")
public class OrderController {

    private final BithumbFeignService bithumbFeignService;
    private final BinanceFeignService binanceFeignService;

    @GetMapping("/chance")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "빗썸 주문 가능 정보 조회", description = "market 코드(예: KRW-BTC)에 대한 주문 가능 정보 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = OrderChanceResponseDto.class)))
    public ApiResponse<OrderChanceResponseDto> getOrderChance(
            @RequestParam(defaultValue = "KRW-BTC") String market) {
        return ApiResponse.ok(bithumbFeignService.getOrderChance(market));
    }

    @GetMapping("/binance/server-time")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "바이낸스 서버 시각 조회", description = "바이낸스 서버 시각을 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = BinanceServerTimeDto.class)))
    public ApiResponse<BinanceServerTimeDto> getBinanceServerTime() {
        return ApiResponse.ok(binanceFeignService.getServerTime());
    }

    @GetMapping("/binance/ticker-prices")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "바이낸스 티커 가격 목록 조회", description = "모든 심볼의 현재 가격 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = List.class)))
    public ApiResponse<List<BinanceTickerPriceDto>> getBinanceTickerPrices() {
        return ApiResponse.ok(binanceFeignService.getTickerPrices());
    }

    @GetMapping("/binance/exchange-info")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "바이낸스 거래소 정보 조회", description = "심볼 및 필터 등 거래소 메타데이터 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = BinanceExchangeInfoResponseDto.class)))
    public ApiResponse<BinanceExchangeInfoResponseDto> getBinanceExchangeInfo() {
        return ApiResponse.ok(binanceFeignService.getExchangeInfo());
    }

    @GetMapping("/binance/account")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "바이낸스 계좌 상태 조회", description = "계좌 거래 가능 여부 및 잔고 목록을 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = BinanceAccountInfoDto.class)))
    public ApiResponse<BinanceAccountInfoDto> getBinanceAccount(
            @RequestParam("apiKey") String apiKey,
            @RequestParam("timestamp") long timestamp,
            @RequestParam("signature") String signature) {
        return ApiResponse.ok(binanceFeignService.getAccountInfo(apiKey, timestamp, signature));
    }

    @PostMapping("/binance/order/limit")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "바이낸스 지정가 주문", description = "지정가 매수/매도 주문을 생성")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = BinanceOrderResponseDto.class)))
    public ApiResponse<BinanceOrderResponseDto> placeBinanceLimitOrder(
            @RequestParam("apiKey") String apiKey,
            @RequestParam("symbol") String symbol,
            @RequestParam("side") String side,
            @RequestParam("timeInForce") String timeInForce,
            @RequestParam("quantity") String quantity,
            @RequestParam("price") String price,
            @RequestParam("timestamp") long timestamp,
            @RequestParam("signature") String signature) {
        return ApiResponse.ok(binanceFeignService.placeLimitOrder(apiKey, symbol, side, timeInForce, quantity,
                price, timestamp, signature));
    }
}
