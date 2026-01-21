package com.yieldflow.management.global.client.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 빗썸 입출금 현황 응답 DTO
 */
public record BithumbDepositWithdrawalResponse(
        String status,
        String message,
        Data data) {
    public record Data(
            List<Transaction> deposit,
            List<Transaction> withdrawal) {
    }

    public record Transaction(
            String search,
            String transfer_date,
            String order_currency,
            String payment_currency,
            String units,
            String price,
            BigDecimal krw_amount,
            BigDecimal fee,
            String order_balance,
            String order_id) {
    }
}
