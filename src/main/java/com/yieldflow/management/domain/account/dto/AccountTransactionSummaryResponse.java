package com.yieldflow.management.domain.account.dto;

import com.yieldflow.management.domain.account.entity.AccountTransaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * 입출금 현황 요약 응답 DTO
 */
public record AccountTransactionSummaryResponse(
        List<AccountTransactionResponse> deposits,
        List<AccountTransactionResponse> withdrawals,
        TransactionSummary depositSummary,
        TransactionSummary withdrawalSummary,
        int totalCount) {
    public record TransactionSummary(
            int count,
            BigDecimal totalAmount,
            BigDecimal totalFee) {
    }

    public static AccountTransactionSummaryResponse from(
            List<AccountTransaction> deposits,
            List<AccountTransaction> withdrawals) {

        var depositResponses = AccountTransactionResponse.from(deposits);
        var withdrawalResponses = AccountTransactionResponse.from(withdrawals);

        var depositSummary = new TransactionSummary(
                deposits.size(),
                deposits.stream()
                        .map(AccountTransaction::getKrwAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                deposits.stream()
                        .map(AccountTransaction::getFee)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        var withdrawalSummary = new TransactionSummary(
                withdrawals.size(),
                withdrawals.stream()
                        .map(AccountTransaction::getKrwAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                withdrawals.stream()
                        .map(AccountTransaction::getFee)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        return new AccountTransactionSummaryResponse(
                depositResponses,
                withdrawalResponses,
                depositSummary,
                withdrawalSummary,
                deposits.size() + withdrawals.size());
    }
}
