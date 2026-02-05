package com.yieldflow.management.global.external.bithumb.dto;

public record BithumbMarketCodeResponseDto(

        String market,
        String koreanName,
        String englishName,
        String marketWarning) {

}
