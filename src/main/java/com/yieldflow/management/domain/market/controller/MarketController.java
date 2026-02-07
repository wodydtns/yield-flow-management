package com.yieldflow.management.domain.market.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.domain.market.service.MarketService;
import com.yieldflow.management.global.external.bithumb.dto.BithumbMarketCodeResponseDto;
import com.yieldflow.management.global.external.bithumb.dto.BithumbVirtualAssetWarning;
import com.yieldflow.management.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/market-codes")
    public ApiResponse<List<BithumbMarketCodeResponseDto>> getMarketCodes() {
        return ApiResponse.ok(marketService.getMarketCodes());
    }

    @GetMapping("/virtual-asset-warning")
    public ApiResponse<List<BithumbVirtualAssetWarning>> getVirtualAssetWarning() {
        return ApiResponse.ok(marketService.getVirtualAssetWarning());
    }
}
