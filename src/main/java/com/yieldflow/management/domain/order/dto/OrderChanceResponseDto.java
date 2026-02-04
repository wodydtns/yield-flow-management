package com.yieldflow.management.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OrderChanceResponseDto(
        @JsonProperty("bid_fee") String bidFee,
        @JsonProperty("ask_fee") String askFee,
        @JsonProperty("maker_bid_fee") String makerBidFee,
        @JsonProperty("maker_ask_fee") String makerAskFee,
        Market market,
        @JsonProperty("bid_account") Account bidAccount,
        @JsonProperty("ask_account") Account askAccount) {
    public record Market(
            String id,
            String name,
            @JsonProperty("order_types") List<String> orderTypes,
            @JsonProperty("order_sides") List<String> orderSides,
            @JsonProperty("bid_types") List<String> bidTypes,
            @JsonProperty("ask_types") List<String> askTypes,
            MarketSupport bid,
            MarketSupport ask,
            @JsonProperty("max_total") String maxTotal,
            String state) {
    }

    public record MarketSupport(
            String currency,
            @JsonProperty("price_unit") String priceUnit,
            @JsonProperty("min_total") String minTotal) {
    }

    public record Account(
            String currency,
            String balance,
            String locked,
            @JsonProperty("avg_buy_price") String avgBuyPrice,
            @JsonProperty("avg_buy_price_modified") boolean avgBuyPriceModified,
            @JsonProperty("unit_currency") String unitCurrency) {
    }
}
