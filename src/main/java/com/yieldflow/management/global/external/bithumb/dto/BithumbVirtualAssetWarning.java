package com.yieldflow.management.global.external.bithumb.dto;

public record BithumbVirtualAssetWarning(
        String market,
        String warningType,
        String warningStep,
        String endDate

) {

}
