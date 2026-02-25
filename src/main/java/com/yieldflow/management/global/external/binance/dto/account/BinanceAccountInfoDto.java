package com.yieldflow.management.global.external.binance.dto.account;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BinanceAccountInfoDto(
        @JsonProperty("makerCommission") int makerCommission,
        @JsonProperty("takerCommission") int takerCommission,
        @JsonProperty("buyerCommission") int buyerCommission,
        @JsonProperty("sellerCommission") int sellerCommission,
        @JsonProperty("canTrade") boolean canTrade,
        @JsonProperty("canWithdraw") boolean canWithdraw,
        @JsonProperty("canDeposit") boolean canDeposit,
        @JsonProperty("balances") List<Balance> balances) {

    public record Balance(
            String asset,
            String free,
            String locked) {
    }
}
