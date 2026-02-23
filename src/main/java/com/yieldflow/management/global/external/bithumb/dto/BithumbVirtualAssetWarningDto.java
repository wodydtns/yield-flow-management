package com.yieldflow.management.global.external.bithumb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BithumbVirtualAssetWarningDto(
        String market,
        @JsonProperty("warning_type") String warningType,
        @JsonProperty("warning_step") String warningStep,
        @JsonProperty("end_date") String endDate) {
}
