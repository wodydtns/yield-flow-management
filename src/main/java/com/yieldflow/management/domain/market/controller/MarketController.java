package com.yieldflow.management.domain.market.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.domain.market.service.MarketService;
import com.yieldflow.management.global.external.bithumb.dto.BithumbMarketCodeResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/market-codes")
    public List<BithumbMarketCodeResponseDto> getMarketCodes() {

        return marketService.getMarketCodes();
    }
}
