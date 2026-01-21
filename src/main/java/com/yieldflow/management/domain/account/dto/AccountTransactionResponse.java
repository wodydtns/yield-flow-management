package com.yieldflow.management.domain.account.dto;

import com.yieldflow.management.domain.account.entity.AccountTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 계좌 입출금 거래 내역 응답 DTO
 */
public record AccountTransactionResponse(
        Long id,
        String orderId,
        String transactionType,
        String orderCurrency,
        String paymentCurrency,
        BigDecimal units,
        BigDecimal price,
        BigDecimal krwAmount,
        BigDecimal fee,
        BigDecimal orderBalance,
        LocalDateTime transferDate,
        LocalDateTime createdAt) {
    public static AccountTransactionResponse from(AccountTransaction transaction) {
        return new AccountTransactionResponse(
                transaction.getId(),
                transaction.getOrderId(),
                transaction.getTransactionType().getDescription(),
                transaction.getOrderCurrency(),
                transaction.getPaymentCurrency(),
                transaction.getUnits(),
                transaction.getPrice(),
                transaction.getKrwAmount(),
                transaction.getFee(),
                transaction.getOrderBalance(),
                transaction.getTransferDate(),
                transaction.getCreatedAt());
    }

    public static List<AccountTransactionResponse> from(List<AccountTransaction> transactions) {
        return transactions.stream()
                .map(AccountTransactionResponse::from)
                .toList();
    }

    /**
     * 입출금 거래 내역 요약 응답 DTO
     */
    public record AccountTransactionSummaryResponse(
            List<AccountTransactionResponse> deposits,
            List<AccountTransactionResponse> withdrawals) {

        public static AccountTransactionSummaryResponse from(
                List<AccountTransaction> deposits,
                List<AccountTransaction> withdrawals) {
            return new AccountTransactionSummaryResponse(
                    AccountTransactionResponse.from(deposits),
                    AccountTransactionResponse.from(withdrawals));
        }
    }
}
