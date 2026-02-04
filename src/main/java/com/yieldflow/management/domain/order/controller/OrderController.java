package com.yieldflow.management.domain.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.domain.order.dto.OrderChanceResponseDto;
import com.yieldflow.management.global.external.bithumb.service.BithumbService;
import com.yieldflow.management.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final BithumbService bithumbService;

    @GetMapping("/chance")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderChanceResponseDto> getOrderChance(
            @RequestParam(defaultValue = "KRW-BTC") String market) {
        return ApiResponse.ok(bithumbService.getOrderChance(market));
    }
}
