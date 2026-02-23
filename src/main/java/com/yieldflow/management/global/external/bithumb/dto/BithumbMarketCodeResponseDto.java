package com.yieldflow.management.global.external.bithumb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BithumbMarketCodeResponseDto(

        String market,
        @JsonProperty("korean_name") String koreanName,
        @JsonProperty("english_name") String englishName,
        @JsonProperty("market_warning") String marketWarning) {

}
